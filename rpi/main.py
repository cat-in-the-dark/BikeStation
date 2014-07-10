from rpi import bluetooth_server
from rpi.config import log


def main():
    log.debug("here am I")
    bluetooth_server.start()

if __name__ == '__main__':
    main()
