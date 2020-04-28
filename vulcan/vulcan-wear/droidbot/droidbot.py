# This file contains the main class of droidbot
# It can be used after AVD was started, app was installed, and adb had been set up properly
# By configuring and creating a droidbot instance,
# droidbot will start interacting with Android in AVD like a human
import logging
import os
import sys
import pkg_resources
import shutil
from threading import Timer

from .device import Device
from .app import App
from .env_manager import AppEnvManager
from .input_manager import InputManager

import bork.config as GV


class DroidBot(object):
    """
    The main class of droidbot
    """
    # this is a single instance class
    instance = None

    def __init__(self,
                 app_path=None,
                 device_serial=None,
                 is_emulator=False,
                 output_dir=None,
                 env_policy=None,
                 policy_name=None,
                 random_input=False,
                 script_path=None,
                 event_count=None,
                 event_interval=None,
                 timeout=None,
                 keep_app=None,
                 keep_env=False,
                 cv_mode=False,
                 debug_mode=False,
                 profiling_method=None,
                 grant_perm=False,
                 enable_accessibility_hard=False,
                 master=None,
                 humanoid=None,
                 ignore_ad=False,
                 replay_output=None,
                 # new params
                 device2_serial=None,
                 wear_device=0,
                 no_install=False):
        """
        initiate droidbot with configurations
        :return:
        """
        logging.basicConfig(level=logging.DEBUG if debug_mode else logging.INFO, format=GV.LOG_FORMAT)

        self.logger = logging.getLogger('DroidBot')
        DroidBot.instance = self

        self.output_dir = output_dir
        if output_dir is not None:
            if not os.path.isdir(output_dir):
                os.makedirs(output_dir)
            html_index_path = pkg_resources.resource_filename("droidbot", "resources/index.html")
            stylesheets_path = pkg_resources.resource_filename("droidbot", "resources/stylesheets")
            target_stylesheets_dir = os.path.join(output_dir, "stylesheets")
            if os.path.exists(target_stylesheets_dir):
                shutil.rmtree(target_stylesheets_dir)
            shutil.copy(html_index_path, output_dir)
            shutil.copytree(stylesheets_path, target_stylesheets_dir)

        self.timeout = timeout
        self.timer = None
        self.keep_env = keep_env
        self.keep_app = keep_app

        self.device = None
        self.app = None
        self.droidbox = None
        self.env_manager = None
        self.input_manager = None
        self.enable_accessibility_hard = enable_accessibility_hard
        self.humanoid = humanoid
        self.ignore_ad = ignore_ad
        self.replay_output = replay_output

        self.enabled = True

        # wearable device (bork)
        self.device_paired = None
        self.wear_device = wear_device
        self.no_install = no_install

        try:

            # sanity check
            # device2_serial is mandatory if the paired device is specified as the wearable
            if self.wear_device == 2:
                if device_serial is None:
                    raise Exception("Paired device was not defined and is expected.")

            # setup main device
            self.device = Device(
                device_serial=device_serial,
                is_emulator=is_emulator,
                output_dir=self.output_dir,
                cv_mode=cv_mode,
                grant_perm=grant_perm,
                enable_accessibility_hard=self.enable_accessibility_hard,
                humanoid=self.humanoid,
                ignore_ad=ignore_ad,
                is_wearable=True if wear_device == 1 else False)

            self.app = App(app_path, output_dir=self.output_dir)

            # NOTE: /dcsl/ Setup secondary device (paired device)
            #       If the exploration is done from the mobile, the paired device is the wearable; however, in case
            #       that no `wear_device_serial` is provided, there is no need to create this additional device.
            if device2_serial is not None:
                self.device_paired = Device(
                    device_serial=device2_serial,
                    output_dir=self.output_dir,
                    is_wearable=True,
                    package_name=self.app.package_name)
            else:
                self.device_paired = None

            self.env_manager = AppEnvManager(
                device=self.device,
                app=self.app,
                env_policy=env_policy)

            print('\eba\ droidbot.py - __init__: Before create an instance of InputManager')
            print('\eba\ droidbot.py - __init__: policy_name {}'.format(policy_name))

            self.input_manager = InputManager(
                device=self.device,
                app=self.app,
                policy_name=policy_name,
                random_input=random_input,
                event_count=event_count,
                event_interval=event_interval,
                script_path=script_path,
                profiling_method=profiling_method,
                master=master,
                replay_output=replay_output,
                device_paired=self.device_paired,
                wear_device=self.wear_device)


        except Exception:
            import traceback
            traceback.print_exc()
            self.stop()
            sys.exit(-1)

    @staticmethod
    def get_instance():
        if DroidBot.instance is None:
            print("Error: DroidBot is not initiated!")
            sys.exit(-1)
        return DroidBot.instance

    def start(self):
        """
        start interacting (exploration)
        :return:
        """
        if not self.enabled:
            return
        self.logger.info("Starting DroidBot")
        try:
            if self.timeout > 0:
                self.timer = Timer(self.timeout, self.stop)
                self.timer.start()

            self.logger.info("Connecting to main device %s." %
                             (self.device.serial))
            self.device.set_up()

            if not self.enabled:
                return
            self.device.connect()

            if not self.enabled:
                return

            # FIXME: eba 06/19/19 Check that sensors are retrieved/stored
            # RE:    eba 07/06/19 Now this is done in the device class
            # Retrieving the sensors installed in the device
            # self.device.get_sensors()

            if not self.no_install:
                self.device.install_app(self.app)
            else:
                self.logger.info("Skipping install of {%s}" % self.app.package_name)

            if not self.enabled:
                return
            self.env_manager.deploy()

            self.logger.info("Target app {%s}." % self.app.package_name)

            # Establish connection to paired device only if its defined.
            if self.device_paired is not None:
                self.logger.info("Connecting to paired device %s." %
                                 (self.device_paired.serial))

                # connect to wearable device
                self.device_paired.set_up()

                if not self.enabled:
                    return
                self.device_paired.connect()

                # FIXME: eba 06/19/19 Check that sensors are retrieved/stored
                # RE:    eba 07/06/19 Now this is done in the device class
                # Retrieving the sensors installed in the device
                # self.device_paired.get_sensors()

                self.logger.info("Connected to wearable device %s." % self.device_paired.serial)
                # self.logger.info("Sensor list in wearable device: %s" % self.wear_device.sensors)

            if not self.enabled:
                return
            if self.droidbox is not None:
                # start exploration on device (original)
                self.droidbox.set_apk(self.app.app_path)
                self.droidbox.start_unblocked()

                print('\eba\ droidbot.py - start: calling self.input_manager.start()')

                self.input_manager.start()
                self.droidbox.stop()
                self.droidbox.get_output()
            else:
                self.input_manager.start()
        except KeyboardInterrupt:
            self.logger.info("Keyboard interrupt.")
            pass
        except Exception:
            import traceback
            traceback.print_exc()
            self.stop()
            sys.exit(-1)

        self.stop()
        self.logger.info("DroidBot Stopped")

    def stop(self):
        self.enabled = False
        if self.timer and self.timer.isAlive():
            self.timer.cancel()
        if self.env_manager:
            self.env_manager.stop()
        if self.input_manager:
            self.input_manager.stop()
        if self.droidbox:
            self.droidbox.stop()
        if self.device_paired:
            self.device_paired.disconnect()
        if self.device:
            self.device.disconnect()
        if not self.keep_env:
            self.device.tear_down()
            if self.device_paired:
                self.device_paired.tear_down()
        if not self.keep_app:
            if not self.no_install:
                self.device.uninstall_app(self.app)
            else:
                self.logger.info("Skipping uninstall of {%s}" % self.app.package_name)
        if hasattr(self.input_manager.policy, "master") and \
           self.input_manager.policy.master:
            import xmlrpc.client
            proxy = xmlrpc.client.ServerProxy(self.input_manager.policy.master)
            proxy.stop_worker(self.device.serial)


class DroidBotException(Exception):
    pass
