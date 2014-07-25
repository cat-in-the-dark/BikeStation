import RPi.GPIO as GPIO
from rpi.config import log

_GPIO_INITED = False
OPENED = False


def init_gpio():
    GPIO.setmode(GPIO.BOARD)
    GPIO.setup(5, GPIO.OUT)
    global _GPIO_INITED
    _GPIO_INITED = True


def open_gate(gate_id):
    log.debug("Gate {} opened".format(gate_id - 1))
    OPENED = True
    # turn_led(True)


def lock_gate(gate_id):
    log.debug("Gate {} locked".format(gate_id))
    # turn_led(False)
    return True


def turn_led(state):
    if not _GPIO_INITED:
        init_gpio()
    if state:
        GPIO.output(5, True)
    else:
        GPIO.output(5, False)
