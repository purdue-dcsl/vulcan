import logging
import os
import sys
import json
import time
import networkx as nx

# import pylap as p
import matplotlib.pyplot as p

import bork.config as GV
from bork.replay import Replay
from bork.monitor import Monitor
from droidbot.adapter.comando import Comando, ACTION_FUZZ_INTENT, ACTION_FUZZ_NOTIF, ACTION_FUZZ_TEST

from droidbot.device import Device
from droidbot.app import App

# Params
FUZZ_ENABLED = True

class Orchestrator(object):
    """
    Bork. The main class for the orchestrator module.
    Principal module that guide the fuzz testing

    The exploration (replay of events) is done in the main device. Since there's the possibility of having
    exploration in both devices (mobile and wearable), the orchestrator will have to be invoked twice (for each
    of the devices).
    """

    # this is a single instance class
    instance = None

    def __init__(self,
                 app_path=None,
                 device_serial=None,
                 is_emulator=False,
                 output_dir=None,
                 cv_mode=False,
                 grant_perm=False,
                 debug_mode=False,
                 enable_accesibility_hard=False,
                 humanoid=None,
                 device2_serial=None,
                 wear_device=0,
                 replay_output=None
                 ):
        """
        Initiates orchestrator and required configurations
        :param app_path:
        :param device_serial:
        :param wear_device_serial:
        :param state_model:
        :param is_emulator:
        :param output_dir:
        :param cv_mode:
        :param grant_perm:
        :param enable_accesibility_hard:
        :param humanoid:
        :param device2_serial:
        :param wear_device:
        """
        logging.basicConfig(level=logging.DEBUG if debug_mode else logging.INFO, format=GV.LOG_FORMAT)

        self.logger = logging.getLogger('Orchestrator')

        self.keep_env = False
        Orchestrator.instance = self

        self.output_dir = output_dir
        self.device = None
        self.device_paired= None
        self.orchestrator = None
        self.monitor = None
        self.replay_output = replay_output
        self.enable_accesibility_hard = enable_accesibility_hard

        self.enabled = True
        self.comando = None

        try:

            device_adapters = {
                "adb": True,
                "telnet": False,
                "droidbot_app": False,
                "minicap": False,
                "logcat": True,
                "user_input_monitor": True,
                "process_monitor": True,
                "droidbot_ime": False
            }

            # setup to main device
            self.device = Device(
                device_serial,
                is_emulator,
                output_dir=self.output_dir,
                cv_mode=cv_mode,
                grant_perm=grant_perm,
                enable_accessibility_hard=self.enable_accesibility_hard,
                humanoid=humanoid,
                is_wearable = True if wear_device == 1 else False#,
                # adapters=device_adapters
            )
            print ("[ORHC] Device(s) started")

            # app (used for training)
            self.app = App(app_path, output_dir=self.output_dir)
            print ("[ORHC] App started")

            # NOTE: /dcsl/ Setup secondary device (paired device)
            #       This device is optional. If the training was done in the mobile, then we will need a paired
            #       device (wearable). However, in case that no `device2_serial` is provided, this could mean
            #       that the training was done in the wearable and there is no need to instance this additional device.
            if device2_serial is not None:
                self.device_paired = Device(
                    device_serial=device2_serial,
                    output_dir=self.output_dir,
                    is_wearable=True,
                    package_name=self.app.package_name,
                    adapters=device_adapters)
            else:
                self.device_paired = None

            # replay module
            self.replay = Replay(
                device_serial=device_serial,
                is_emulator=is_emulator,
                output_dir=output_dir
            )
            print ("[ORHC] Replay Module started")

            # NOTE: /dcsl/ Socket command interface
            #       This adapter is used to communicate fuzz command to the target application.
            if wear_device == 1:
                self.comando = Comando(self.device)
                print("[ORHC] Command interface started on %s" % self.device.serial)
            else:
                self.comando = Comando(self.device_paired)
                print("[ORHC] Command interface started on %s" % self.device_paired.serial)


        except Exception:
            import traceback
            traceback.print_exc()
            self.stop()
            sys.exit(-1)

    @staticmethod
    def get_instance():
        if Orchestrator.instance is None:
            print("Error: Orchestrator is not initiated!")
            sys.exit(-1)
        return Orchestrator.instance

    def start(self):
        """
        Run the orchestrator (start the fuzz)
        """
        if not self.enabled:
            return
        self.logger.info("Starting fuzzer")

        try:

            # setup/connect to mobile device
            self.device.set_up()

            if not self.enabled:
                return

            self.device.connect()

            # setup/connect to wearable device
            if self.device_paired:
                self.device_paired.set_up()

                if not self.enabled:
                    return

                self.device_paired.connect()

            # FIXME: /eba/ 7/27/19 Enable monitor
            # # start monitor
            # self.monitor = Monitor(wear_device=self.wear_device)
            # self.monitor.run()

            if not self.enabled:
                return

            # establish connection with device
            self.comando.connect()

            if not self.enabled:
                return

            self.run()

        except KeyboardInterrupt:
            self.logger.info("Keyboard interrupt.")
            pass
        except Exception:
            import traceback
            traceback.print_exc()
            self.stop()
            sys.exit(-1)

        self.stop()
        self.logger.info("Orchestrator Stopped")


    def run(self):
        # Load state model graph
        self.G = nx.DiGraph()
        self.__load_graph()

        # temp
        # nx.draw(self.G)
        # self.__loop()
        # p.show()
        # sys.exit(0)

        # for each state:
        #  (1) call replay module to steer the device
        #  (2) do a fuzz campaign according to the state

        # iterate model
        self.__loop()

    def stop(self):
        """
        Gracefully shutdown or close open resources (e.g. devices)
        :return:
        """
        if self.comando:
            self.comando.disconnect()
        if self.device:
            self.device.disconnect()
        if self.device_paired:
            self.device_paired.disconnect()
        if not self.keep_env:
            # self.monitor.stop()
            self.device.tear_down()
            if self.device_paired:
                self.device_paired.tear_down()

    def __loop(self):
        """
        Iterate thru the state model
        :return:
        """

        nodes = self.G.nodes()

        print ("looping")
        # set root source (to initiate the exploration)
        source = self.G.nodes()[self.source]

        print(source)

        # FIXME: \eba\ 7/27/19 Definetely replace this form to start the app, since is not working and is too much overhead
        # Every traverse of the graph starts from the main activity of the app
        self.device.start_app(self.app)

        print ("\eba\ Replaying to replay events ...")

        # FIXME. This probably will require some work in the near future
        # The basic problem is the granularity of state and the relation of the state with the transitions, because is
        # possible to have in the same state more than one activity (if we only consider sensor/communication). This
        # problem will be more relevant in the mobile device.
        edges = nx.dfs_edges(self.G, source['name'])
        for edge in edges:
            # steer to this state
            data = self.G.get_edge_data(*edge)
            # replay event
            event_json_file_path = "%sevents/event_%s.json" % (self.replay_output, data['tag'])
            print(event_json_file_path)
            self.replay.run(event_json_file_path)

            # do fuzz
            self.__fuzz(data)


        # self.__fuzz_intent(self.FUZZER_ACTION_START)

    def __load_graph(self):
        """
        Load state model graph
        :param digraph: the json file that represents the state model of the app
        :return:
        """

        # read model from json
        input = os.path.join(self.replay_output, "utg.js")
        with open(input) as f:

            # skip first line ("var utg = ")
            f.readline()
            data = json.load(f)

            nodes = data['nodes']
            edges = data['edges']

            for node in nodes:
                label = 'label_{}'.format(node['id'])
                self.G.add_node(node['id'], label=label, name=node['id'])

            for edge in edges:
                label = 'label-{}-{}_{}'.format(edge['from'], edge['to'], edge['id'])
                self.G.add_edge(edge['from'], edge['to'], label=label, wear_events=['wear_events'], tag=edge['tag'])

            # for node in self.G.nodes():
            #     print(node)
            #     for edge in self.G.edges(node):
            #         print(edge)

            # source node
            self.source = data['first_activity']

    # TODO: (3/27/19) This is probably better to move in a separate file.
    # Since we are going to have richer fuzzing strategy, is better to have all the fuzzing
    # strategy in a separate file, or at least the interface to interact/call/invoke them.
    def __fuzz(self, data):

        if ( FUZZ_ENABLED ):
            print("\eba\ do fuzzing")
        else:
            return

        type = 1

        # NOTE: /dcsl/ Fuzzing Strategies
        #       The application of the strategies depends on the state of the target device.

        if ( type == 1 ):
            # Intent Fuzzing Strategy.
            # This campaign is only applied to the initial states or a state marked as `vulnerable`.
            action = {
                "target": self.app.get_package_name(),
                "action": ACTION_FUZZ_INTENT
            }
            self.__fuzz_intent(action)
        elif ( type == 2 ):
            # Communication Fuzzing Strategy.
            # This require a interface with frida server (incl. instrumentation of the target app).
            pass
        elif ( type == 3 ):
            # FCM (Firebase Cloud Messaging)
            # This either require an interface with frida server (incl. instrumentation of the target app)
            # or/and root access on the target device.
            pass


    def __fuzz_intent(self, action):
        """
        Invoke intent injection campaign.
        :return:
        """
        rtn = self.comando.send(action)
        print('status: %s' % rtn)


