import subprocess
import logging
from .adapter import Adapter

from bork import utils

class Logcat(Adapter):
    """
    A connection with the target device through logcat.
    """

    def __init__(self, device=None, out_file=None, package_name=None, is_wearable=False):
        """
        initialize logcat connection
        :param device: a Device instance
        """
        self.logger = logging.getLogger(self.__class__.__name__)
        if device is None:
            from droidbot.device import Device
            device = Device()
        self.device = device
        self.connected = False
        self.process = None
        if device.output_dir is None:
            self.out_file = None
        else:
            if out_file is not None:
                self.out_file = "%s/%s" % (device.output_dir, out_file)
            else:
                self.out_file = "%s/logcat.txt" % device.output_dir
        self.package_name = package_name
        self.is_wearable = is_wearable

        # NOTE: /dcsl/ Wear Event Mapper.
        #       Maps an observable state in the wearable (e.g., sensor activity, intra-device comm.)
        #       with a sequence of UI events (either in the mobile device or wearable device).
        self.wear_events = []

    def connect(self):
        self.device.adb.run_cmd("logcat -c")
        self.process = subprocess.Popen(["adb", "-s", self.device.serial, "logcat", "-v", "time", "VVV", "threadtime"],
                                        stdin=subprocess.PIPE,
                                        stderr=subprocess.PIPE,
                                        stdout=subprocess.PIPE)
        import threading
        listen_thread = threading.Thread(target=self.handle_output)
        listen_thread.start()

    def disconnect(self):
        self.connected = False
        if self.process is not None:
            self.process.terminate()

    def check_connectivity(self):
        return self.connected

    def handle_output(self):
        self.connected = True

        f = None
        if self.out_file is not None:
            f = open(self.out_file, 'w')

        while self.connected:
            if self.process is None:
                continue
            line = self.process.stdout.readline()
            if not isinstance(line, str):
                line = line.decode()
            self.parse_line(line)
            if f is not None:
                f.write(line)
        if f is not None:
            f.close()
        print("[CONNECTION] %s: %s is disconnected" % (self.device.get_model_number(), self.__class__.__name__))

    def parse_line(self, logcat_line):
        """
        Parse the logcat to look for traces of the observable states in the device. This is just done for
        wearable devices.
        :param logcat_line:
        :return:
        """
        if self.is_wearable:
            wear_event = utils.parse_log_wear(logcat_line, self.package_name)

            # append only observable events
            if wear_event is not None:
                self.wear_events.append(wear_event)

    def get_mappings(self):
        """
        Get events (if any) triggered on the device (just for wearable devices).
        :return:
        """
        events = self.wear_events
        self.wear_events = []

        return events
