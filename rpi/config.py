import logging

logging.basicConfig(filename='station.log', format='%(levelname)s : %(name)s : %(message)s', level=logging.DEBUG)
log = logging.getLogger("BikeStation")
