# helper file of bork
# start the orchestrator module (main module) in the fuzzer.
import argparse
import bork.config as GV

from bork.orchestrator import Orchestrator

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
    parser.add_argument("-a", action="store", dest="apk_path", required=True,
                        help="The file path to target APK")
    parser.add_argument("-o", action="store", dest="output_dir",
                        help="The directory of output")
    parser.add_argument("-is_emulator", action="store_true", dest="is_emulator",
                        help="Declare the target device to be an emulator, which would be"
                             "treated specially by Bork")

    # paired device, which usually is a wearable (e.g., smartwatch)
    parser.add_argument("-d2", action="store", dest="device2_serial", required=False,
                        help="The serial number of the paired device (use `adb devices` to find). " +
                             "Usually is a wearable device")
    # indicates which is the wearable device (either device_serial or device2_serial)
    parser.add_argument("-wear", action="store", dest="wear_device", required=False, choices=[0, 1, 2], type=int,
                        const=0, nargs='?',
                        help="Declare which is the wearable device (either 0, 1 or 2)")
    # indicates the directory with the training data
    parser.add_argument("-replay_output", action="store", dest="replay_output",
                        help="The droidbot output directory being replayed.")

    options = parser.parse_args()
    return options


def main():
    """
        the main function
        it starts the main module of the fuzzer, the orchestrator, which will guide the
        fuzz test using the model.

        Usage:
        python -m bork.start_orchestrator -d 84B7N16302002652 -a apks/mobile-apk/seven.apk  -d2 127.0.0.1:4444 -wear 2
        python -m bork.start_orchestrator -d 0268936e5bed29a3 -a apks/mobile-apk/seven.apk  -d2 127.0.0.1:4444 -wear 2

    """
    opts = parse_args()

    # Logging Settings
    GV.LOG_FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'

    orchestrator = Orchestrator(
        device_serial=opts.device_serial,
        app_path=opts.apk_path,
        is_emulator=opts.is_emulator,
        output_dir=opts.output_dir,
        device2_serial=opts.device2_serial,
        wear_device=opts.wear_device,
        replay_output=opts.replay_output
    )
    orchestrator.start()


if __name__ == "__main__":
    main()
