import re
import sys

from bork import utils
from droidbot.adapter.logcat import Logcat

# TODO: /dcsl/ This class is no longer useful. Gracefully remove it.
class WearLogcat(Logcat):
    """
    Wear Event Mapper.
    Maps an observable state in the wearable (e.g., sensor activity, intra-device comm.) with a sequence of
    UI events (either in the mobile device or wearable device).
    """
    def connect(self):
        self.wear_events = []
        Logcat.connect(self)

    def parse_line(self, logcat_line):
        """
        Parse the logcat to look for traces of the observables states in the wearable device.
        :param logcat_line:
        :return:
        """
        wear_event = utils.parse_log_wear(logcat_line, self.package_name)

        # append only observable events
        if wear_event is not None:
            self.wear_events.append(wear_event)

    def get_mappings(self):
        """
        Get events (if any) triggered on the wearable device
        :return:
        """
        events = self.wear_events
        self.wear_events = []

        return events


