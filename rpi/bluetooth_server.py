import bluetooth
import json
import threading
from rpi.config import log
import requests
from rpi.constants import SERVER_NAME, SERVER_UUID, BUFFER_SIZE, STATUS, OK, ON_BIKE, TRUE, COMMAND, \
    HELLO, LOGIN, PIN, GET_BIKES_LIST, FALSE, BIKE_LIST, SELECT_BIKE, RETURN_BIKE, WEB_SERVER_ADDR, WEB_SERVER_PORT, \
    GET_BIKES, ERROR, MESSAGE, DATA, HTTP_STATUS_UNAUTHORIZED, HTTP_STATUS_OK, GET_HAS_RENT, BIKE_ID, POST_START_RENT, \
    HTTP_STATUS_ACCEPTED, GATE_NUMBER, HTTP_STATUS_FORBIDDEN, POST_CLOSE_RENT, STATION_ID_KEY, \
    STATION_ID_VALUE
from rpi.hardware import open_gate, get_opened_gate, lock_gate


class ClientThread(threading.Thread):
    def __init__(self, client_sock, client_info):
        threading.Thread.__init__(self)
        self._credentials = None
        self.sock = client_sock
        self.info = client_info
        self._endpoint = WEB_SERVER_ADDR + ":{}".format(WEB_SERVER_PORT)

    def recv(self):
        recv_data = self.sock.recv(BUFFER_SIZE)
        log.debug("Received: %s" % recv_data)
        return recv_data

    def send(self, data):
        self.sock.send("%s\n\r" % data)
        log.debug("Sent: %s" % data)

    def run(self):
        try:
            on_bike = False
            while True:
                recv_data = self.recv()
                parsed_data = json.loads(recv_data)
                if parsed_data[COMMAND] == HELLO:
                    login = parsed_data[LOGIN]
                    pin = parsed_data[PIN]
                    self._credentials = {
                        LOGIN: login,
                        PIN: pin,
                        STATION_ID_KEY: STATION_ID_VALUE
                    }
                    has_rent_resp = requests.get(self._endpoint + GET_HAS_RENT, params=self._credentials).json()
                    log.debug(has_rent_resp)
                    if has_rent_resp[STATUS] is not None:
                        if has_rent_resp[STATUS] == HTTP_STATUS_UNAUTHORIZED:
                            self.send(json.dumps({STATUS: ERROR, MESSAGE: has_rent_resp[MESSAGE]}))
                            break
                        elif has_rent_resp[STATUS] == HTTP_STATUS_OK:
                            self.send(json.dumps({STATUS: OK, ON_BIKE: has_rent_resp[DATA]}))
                            continue
                    else:
                        log.error("Response has not status")

                if parsed_data[COMMAND] == GET_BIKES_LIST:
                    get_bikes_resp = requests.get(self._endpoint + GET_BIKES, params=self._credentials).json()
                    if get_bikes_resp[STATUS] is not None:
                        if get_bikes_resp[STATUS] == HTTP_STATUS_OK:
                            bikes_list = get_bikes_resp[DATA]
                            log.debug(bikes_list)
                            self.send(json.dumps({STATUS: OK, BIKE_LIST: [bike["id"] for bike in bikes_list]}))

                if parsed_data[COMMAND] == SELECT_BIKE:
                    if parsed_data[BIKE_ID] is not None:
                        bike_id = parsed_data[BIKE_ID]
                        post_data = {BIKE_ID: bike_id}.update(self._credentials)
                        select_bike_resp = requests.post(self._endpoint + POST_START_RENT, data=post_data).json()
                        log.debug(select_bike_resp)
                        if select_bike_resp[STATUS] == HTTP_STATUS_ACCEPTED:
                            open_gate(select_bike_resp[GATE_NUMBER])
                            self.send(json.dumps({STATUS: OK}))
                        elif select_bike_resp[STATUS] == HTTP_STATUS_UNAUTHORIZED \
                                or select_bike_resp[STATUS] == HTTP_STATUS_FORBIDDEN:
                            self.send(json.dumps({STATUS: ERROR, MESSAGE: select_bike_resp[MESSAGE]}))

                if parsed_data[COMMAND] == RETURN_BIKE:
                    # TODO: implement using GPIOs
                    gate_id = get_opened_gate()
                    if lock_gate(gate_id):
                        post_data = {GATE_NUMBER: gate_id}.update(self._credentials)
                        return_bike_resp = requests.post(self._endpoint + POST_CLOSE_RENT, data=post_data).json()
                        if return_bike_resp[STATUS] == HTTP_STATUS_ACCEPTED:
                            self.send(json.dumps({STATUS: OK}))
                        elif return_bike_resp[STATUS] == HTTP_STATUS_FORBIDDEN \
                                or return_bike_resp[STATUS] == HTTP_STATUS_UNAUTHORIZED:
                            self.send(json.dumps({STATUS: ERROR, MESSAGE: return_bike_resp[MESSAGE]}))
                    else:
                        self.send(json.dumps({
                            STATUS: ERROR,
                            MESSAGE: "Cant't lock gate"
                        }))
        except IOError as e:
            log.error('error:%s' % e.message)
        except Exception as e:
            log.error('error:%s' % e.message)
            self.send('error:%s' % e.message)
        self.sock.close()
        log.debug("Disconnected")


def start():
    # run bluetooth server
    server_socket = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
    server_socket.bind(("", bluetooth.PORT_ANY))
    server_socket.listen(1)
    server_port = server_socket.getsockname()[1]

    # run advertise service
    bluetooth.advertise_service(
        server_socket,
        SERVER_NAME,
        service_id=SERVER_UUID,
        service_classes=[SERVER_UUID, bluetooth.SERIAL_PORT_CLASS],
        profiles=[bluetooth.SERIAL_PORT_PROFILE],
    )

    log.info("Server started")
    while True:
        log.info("Wait connection...")
        client_sock, client_info = server_socket.accept()
        log.info("%s : connection accepted", client_info)
        client = ClientThread(client_sock, client_info)
        client.setDaemon(True)
        client.start()
