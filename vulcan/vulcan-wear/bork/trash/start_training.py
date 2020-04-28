# helper file of bork
# start replay module
import argparse

from .wear_logcat import EventMapper
from droidbot.device import Device
from .replay import Replay

def parse_args():
    """
    parse comman line input
    generate options
    :return:
    """
    parser = argparse.ArgumentParser(description="Start frida instrumentation",
                                     formatter_class=argparse.RawTextHelpFormatter)
    parser.add_argument("-m", action="store", dest="device_mobile_serial", required=False,
                        help="The serial number of the target mobile device (use `adb devices` to find)")
    parser.add_argument("-w", action="store", dest="device_wear_serial", required=False,
                        help="The serial number of the target wear device (use `adb devices` to find)")
    parser.add_argument("-o", action="store", dest="output_dir",
                        help="The directory of output")
    parser.add_argument("-is_emulator", action="store_true", dest="is_emulator",
                        help="Declare the target device to be an emulator, which would be"
                             "treated specially by Bork")

    options = parser.parse_args()
    return options



def main():
    """
        the main function
        it starts the respective modules needed to run for the traning phase. The parameters are taken from
        arguments given in cmd line

        Usage:
        python -m bork.start_training -m 84B7N16302002652 -w 127.0.0.1:4444

    """
    opts = parse_args()

    self.device = Device(device_serial=opts.device_wear_serial,
                         output_dir=opts.output_dir)

    event_mapper = EventMapper(
        device=self.d,
        output_dir=opts.output_dir
    )

    event_mapper.connect()

if __name__ == "__main__":
    main()