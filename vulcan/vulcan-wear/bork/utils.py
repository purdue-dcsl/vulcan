import re
import sys
import datetime

import bork.config as CV

from enum import Enum

# -----------------------------------------------------------------------------
# Observable States
# RegEx pattern to parse the logcat file (wearable)
# -----------------------------------------------------------------------------

# Timestamp in the following format MM-DD hi:mi:ss.ms (e.g., 05-31 13:11:07.252)
PATTERN_TS = '(\d{2}-\d{2} \d{2}:\d{2}:\d{2}.\d{3}) '

# (1) Inter-device communication
# The trace identifies a communication between the mobile and wearable; either by message or data sync.

PATTERN_WLS_MSG_RCV = '(.*)/WearableLS\((.*)\): onMessageReceived: ComponentInfo{(.*)} MessageEventParcelable\[(\d+),(.*), size=(\d+)\]'
PATTERN_WLS_DAT_CHG = '(.*)/WearableLS\((.*)\): onDataItemChanged: ComponentInfo{(.*)} (.*), rows=(\d+)'

# (2) Sensor activity
# The trace is different depending of the device. However, if the sensor is activated using Google Fitness API there
# is no trace in the logcat.

#  Motorola Moto 360
PATTERN_SNS_MOTO360 = '(.*)/SensorHAL\((.*)\): poll__activate: sensor\_handle: (\d+) enabled: (\d)'

#  Huawei Watch 2
PATTERN_SNS_HAUWEI_ON  = '(.*)/shd     \((.*)\): ProcessSensorActive sensor:0x(\d+), handle:(\d)'
PATTERN_SNS_HAUWEI_OFF = '(.*)/shd     \((.*)\): ProcessSensorDeactive sensor:0x(\d+), handle:(\d)'

# (3) FCM

# NOTE: /dcsl/ This most probably will have to be done using instrumentation (e.g., Frida). Without any instrumentation
#       there is no visible trace that can be used. 

# (4) Connection with paired device (mobile)
# Trace either when the bluetooth communication is lost or restablished. The communication between the devices happens
# mostly on the bluetooth channel.

PATTERN_BL_DISC = '(.*)/WearBluetooth: Companion device disconnected.'
PATTERN_BL_CONN = '(.*)/WearBluetooth: Companion device connected.'

# NOTE: /dcsl/ Another possible option could be to rely on the information from ACTION_CONNECTION_STATE_CHANGED
#       intent. The intent include more detail information about the bluetooth communication.
#       More info:
#       https://developer.android.com/reference/android/bluetooth/BluetoothAdapter#ACTION_CONNECTION_STATE_CHANGED


# (5) Notifications


PATTERN_LOG = '\s+(\d+)\s+(\d+)\s+([A-Za-z]) ([^:]*):'


class EventType(Enum):
    """
    EventType
    Supported Events (on the wearable side).
    """
    def to_name(self):
        return str(self.name)

    Message = 1
    DataSync = 2
    Sensor = 3

class Level(Enum):
    """
    Level
    Debug level supported.
    """
    def to_name(self):
        return str(self.name)

    Verbose = 0
    Detailed = 1
    Normal = 2
    Silent = 3

def parse_log_wear(line, pkg=None):
    """
    Parse a logcat message looking for observables events on the wearable device.
    :param line: the line from logcat (wearable device)
    :param app: the application being targetted
    :return:
    """
    tags = {}

    # Message Passing
    rx_pattern = re.compile(PATTERN_TS + PATTERN_WLS_MSG_RCV)
    match_pattern = rx_pattern.search(line)
    if match_pattern:
        timestamp = match_pattern.group(1)
        tid = match_pattern.group(3)
        package, component = match_pattern.group(4).split('/')
        action = match_pattern.group(6)

        print("{parse-log-wear} WLS Msg " + timestamp + ' * ' + package + ' * ' + component + ' * ' + action)

        if pkg is not None:
            if pkg == package:
                log_dict = {
                    'id': 'SND/' + action,
                    'timestamp': timestamp,
                    'type': EventType.Message,
                    'action': action,
                    'package': package,
                    'component': component
                }

                return log_dict

    # Data Item Sync
    rx_pattern = re.compile(PATTERN_TS + PATTERN_WLS_DAT_CHG)
    match_pattern = rx_pattern.search(line)
    if match_pattern:
        timestamp = match_pattern.group(1)
        tid = match_pattern.group(3)
        package, component = match_pattern.group(4).split('/')
        dataholder = match_pattern.group(5)
        action = 'none'

        print("{parse-log-wear} WLS Sync " + timestamp + ' * ' + package + ' * ' + component + ' * ' + dataholder)

        if pkg is not None:
            if pkg == package:
                log_dict = {
                    'id': 'DAT/' + action,
                    'timestamp': timestamp,
                    'type': EventType.DataSync,
                    'action': action,
                    'package': package,
                    'component': component
                }
                return log_dict

    # Sensor Activity
    # Motorola Moto 360 Generation 2
    rx_pattern = re.compile(PATTERN_TS + PATTERN_SNS_MOTO360)
    match_pattern = rx_pattern.search(line)
    if match_pattern:
        timestamp = match_pattern.group(1)
        tid = match_pattern.group(3)
        sensor_handle = match_pattern.group(4)
        sensor_status = match_pattern.group(5)

        print("{parse-log-wear} SensorHAL " + timestamp + ' * ' + tid + ' * ' + sensor_handle + ' * ' + sensor_status)

        log_dict = {
            'id': 'HAL/' + sensor_handle,
            'timestamp': timestamp,
            'type': EventType.Sensor,
            'sensor_handle': sensor_handle,
            'sensor_status': sensor_status
        }
        return log_dict

    # Huawei Watch2
    rx_pattern = re.compile(PATTERN_TS + PATTERN_SNS_HAUWEI_ON)
    match_pattern = rx_pattern.search(line)
    if match_pattern:
        timestamp = match_pattern.group(1)
        tid = match_pattern.group(3)
        sensor = '0x'+match_pattern.group(4)
        sensor_handle = match_pattern.group(5)

        print("{parse-log-wear} SensorHAL " + timestamp + ' * ' + tid + ' * ' + sensor + ' * ' + sensor_handle)

        log_dict = {
            'id': 'HAL/' + sensor_handle,
            'timestamp': timestamp,
            'type': EventType.Sensor,
            'sensor': sensor,
            'sensor_handle': sensor_handle,
            'sensor_status': "1"
        }
        return log_dict

    # Deactivation of Sensors
    rx_pattern = re.compile(PATTERN_TS + PATTERN_SNS_HAUWEI_OFF)
    match_pattern = rx_pattern.search(line)
    if match_pattern:
        timestamp = match_pattern.group(1)
        tid = match_pattern.group(3)
        sensor = '0x'+match_pattern.group(4)
        sensor_handle = match_pattern.group(5)

        print(
            "{parse-log-wear} SensorHAL " + timestamp + ' * ' + tid + ' * ' + sensor + ' * ' + sensor_handle)

        log_dict = {
            'id': 'HAL/' + sensor_handle,
            'timestamp': timestamp,
            'type': EventType.Sensor,
            'sensor': sensor,
            'sensor_handle': sensor_handle,
            'sensor_status': "0"
        }
        return log_dict

    # Mobile/Wearable connection
    rx_pattern = re.compile(PATTERN_TS + PATTERN_BL_CONN)
    match_pattern = rx_pattern.search(line)
    if match_pattern:
        timestamp = match_pattern.group(1)

    for tag in tags:
        print(tag)

    return None


def debug(msg, level=Level.Silent):
    if level >= CV.DEBUG_MODE:
        print("{} - {}".format(datetime.datetime.now().strftime("%Y%m%d-%H%M%S"), msg))