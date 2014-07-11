from rpi.config import log

GATES = {1: True}


def open_gate(gate_id):
    log.debug("Gate {} opened".format(gate_id - 1))
    GATES.values()[gate_id - 1] = True


def lock_gate(gate_id):
    if GATES.values()[gate_id - 1]:
        log.debug("Gate {} locked".format(gate_id))
        GATES.values()[gate_id - 1] = False
        return True
    else:
        return False


def get_opened_gate():
    gate_id = 1
    for g in GATES.values():
        if g:
            return gate_id
        else:
            gate_id += 1
