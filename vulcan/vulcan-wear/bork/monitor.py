import logging
import re
import sys
import threading

class Monitor(object):
    """
    Bork. The main class for the monitor module.
    The monitor will iterate thru the logcat to verify the state of the device/application.
    In case of change of state, the monitor will raise a message to the orchestator.
    """

    def __init__(self,
                 wear_device,
                 frequency = 30):
        """
        Initialize monitor
        :param wear_device: wearable device
        :param frequency: frequency or rate of checks to validate the status of the device/application
        """
        self.logger = logging.getLogger(self.__class__.__name__)

        self.wear_device = wear_device
        self.frequency = frequency

        self.last_state = {}
        self.current_state = {}
        self.check_state = False

    def run(self):
        """
        Main loop
        """
        self.logger.info("Starting monitor")
        self.run_check()

    # FIXME: /eba/ 7/5/19 Remove this function from here, this function belongs to device.py
    # def get_state(self, sns_active, sns_all):
    #     """
    #     Returns the state of the device/application based on the sensor activity and the
    #     communication inter-device
    #     :param sns_active: the sensors currently active in the device
    #     :param sns_all: the sensors list installed in the device
    #     :return: the status of the device
    #     """
    #     sensors_all = []
    #     status = {}
    #     for sensor in sns_all:
    #         sns = {
    #             "sensor": sensor["name"],
    #             "handle": sensor["handle"],
    #             "status": "active" if (next(filter(lambda x: x['handle'] == sensor['handle'], sns_active), 0) != 0) else 'deactive'
    #         }
    #         sensors_all.append(sns)
    #     status = {
    #         "sensors": sensors_all
    #     }
    #     return  status

    def check_state_change(self, current_state):
        """
        Validates if the state of the device/application has changed with respect the last state
        :return:
        """
        if self.check_state:
            return not self.current_state(current_state)

    def check_current_state(self, state):
        """
        Validates if the device/application is in an indicated state
        :param state:
        :return: true if the state provided is the same with the last state reported
        """
        # Check sensors status
        for sensor in self.status["sensors"]:
            if (next(filter(lambda x: x['handle'] == sensor['handle'] and x['status'] == sensor['status'], state["sensors"]), 0) == 0):
                return False

        return True

    def run_check(self):
        """
        Check the status of the device/application
        :return:
        """
        threading.Timer(self.frequency, self.run_check).start()

        # Check status of sensors
        sns_active = self.wear_device.get_active_sensors()
        sns_all = self.wear_device.get_sensors()
        self.get_state(sns_active['sensors'], sns_all)
        current_state = self.get_state(sns_active['sensors'], sns_all)
        print (current_state)

    def stop(self):
        """
        Stop monitor
        :return:
        """
        return