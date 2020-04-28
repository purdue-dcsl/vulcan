# helper file of bork
# start replay module
import argparse

from .test_server import Test


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
    parser.add_argument("-a", action="store", dest="package", required=True,
                        help="The package name of the target APK")
    parser.add_argument("-x", action="store", dest="strategy",
                        help="The fuzzing strategy")
    parser.add_argument("-s", action="store", dest="skip", default=0,
                        help="Skip components")

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
    test = Test(device_serial=opts.device_serial,
        package=opts.package,
        strategy=opts.strategy,
        skip=opts.skip)


if __name__ == "__main__":
    main()
