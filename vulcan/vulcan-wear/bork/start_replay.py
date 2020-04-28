# helper file of bork
# start replay module
import argparse

from .replay import Replay

def parse_args():
    """
    parse comman line input
    generate options
    :return:
    """
    parser = argparse.ArgumentParser(description="Start Bork Replay module",
                                     formatter_class=argparse.RawTextHelpFormatter)
    parser.add_argument("-d", action="store", dest="device_serial", required=False,
                        help="The serial number of the target device (use `adb devices` to find)")
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
        it starts the replay module according to the arguments given in cmd line

        Usage:
        python -m bork.start_replay -d 84B7N16302002652  » huawei 6p
        python -m bork.start_replay -d TKQ7N18A10000948  » huawei watch2

    """
    opts = parse_args()

    replay = Replay(
        device_serial=opts.device_serial,
        is_emulator=opts.is_emulator,
        output_dir=opts.output_dir
    )
    replay.run()

if __name__ == "__main__":
    main()
