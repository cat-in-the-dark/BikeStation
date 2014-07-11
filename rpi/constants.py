SERVER_NAME = "BikeStation"
SERVER_UUID = "00001101-0000-1000-8000-00805F9B34FB"
SERVER_ID = "4815162342"
STATION_ID_VALUE = 1

WEB_SERVER_ADDR = "http://127.0.0.1"
WEB_SERVER_PORT = 9292

BUFFER_SIZE = 4096

# bluetooth commands
COMMAND = "command"
HELLO = "hello"
GET_BIKES_LIST = "getBikeList"
SELECT_BIKE = "selectBike"
RETURN_BIKE = "returnBike"

# status commands
STATUS = "status"
OK = "OK"
ERROR = "ERROR"
MESSAGE = "msg"
DATA = "data"

#receiving
LOGIN = "login"
PIN = "PIN"
BIKE_ID = 'bikeId'
GATE_NUMBER = "gateNumber"

#sending
ON_BIKE = "onBike"
BIKE_LIST = "bikeList"
STATION_ID_KEY = "stationId"

TRUE = "true"
FALSE = "false"

# requests commands
GET_BIKES = "/bikes"
GET_HAS_RENT = "/has_rent"
POST_START_RENT = "/start_rent"
POST_CLOSE_RENT = "/close_rent"

HTTP_STATUS_OK = 200
HTTP_STATUS_ACCEPTED = 202
HTTP_STATUS_UNAUTHORIZED = 401
HTTP_STATUS_FORBIDDEN = 403