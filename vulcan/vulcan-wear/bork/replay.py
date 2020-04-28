import json
import logging
import os
import sys

from droidbot.input_event import InputEvent
from droidbot.device import Device

from pprint import pprint

class Replay(object):
    """
    Bork. The main class for the replay module.
    """

    # Testing
    # INPUT_FILE = '/home/ebarsallo/Code/purdue.edu/699-research/android/testing-tools/droidbot/target/output/events/event_2018-10-03_224101.json'
    INPUT_FILE = '/home/ebarsallo/Code/purdue.edu/699-research/android/testing-tools/droidbot-original/target/output/runkeeper.pro/events/event_2018-09-12_192751-eba.json'

    def __init__(self,
                 device_serial=None,
                 is_emulator=False,
                 output_dir=None,
                 cv_mode=False,
                 grant_perm=False,
                 enable_accesibility_hard=False,
                 humanoid=None
                 ):

        self.output_dir=output_dir
        self.enable_accesibility_hard=enable_accesibility_hard
        self.humanoid=humanoid

        self.logger = logging.getLogger('ReplayModule')

        try:
            self.device = Device(
                device_serial,
                is_emulator,
                output_dir=self.output_dir,
                cv_mode=cv_mode,
                grant_perm=grant_perm,
                enable_accessibility_hard=self.enable_accesibility_hard,
                humanoid=humanoid
            )
        except Exception:
            import traceback
            traceback.print_exc()
            self.stop()
            sys.exit(-1)

    def run(self, event=INPUT_FILE):
        """
        run
        """

        # check if json file is not empty
        if os.path.getsize(event) > 0:

            # read file from json
            with open(event) as f:
                data = json.load(f)

                event_dict = data['event']
                event = InputEvent.from_dict(event_dict)
                # pprint(data)
                print(event.event_type)
                self.device.send_event(event)

            # print("List of Sensors")
            # print(self.device.get_sensors())
            # print("Active Sensors")
            # print(self.device.get_active_sensors())

            # status
            # print(self.device.get_status())
        else:
            self.logger.info("Skipping event")



    def stop(self):
        """
        Stop replay module
        :return:
        """
        print("stop")