import bluetooth
import json
from json import JSONDecoder
import threading
from rpi.config import log
import requests
from rpi.constants import SERVER_NAME, SERVER_UUID, BUFFER_SIZE, STATUS, OK, ON_BIKE, TRUE, COMMAND, \
    HELLO, LOGIN, PIN, GET_BIKES_LIST, FALSE, BIKE_LIST, SELECT_BIKE, RETURN_BIKE, WEB_SERVER_ADDR, WEB_SERVER_PORT, \
    GET_BIKES, ERROR, MESSAGE, DATA, HTTP_STATUS_UNAUTHORIZED, HTTP_STATUS_OK, GET_HAS_RENT


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
                        PIN: pin
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
                    self.send(json.dumps({STATUS: OK}))
                if parsed_data[COMMAND] == RETURN_BIKE:
                    self.send(json.dumps({STATUS: OK}))
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
