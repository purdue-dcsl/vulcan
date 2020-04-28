import logging
import socket
import json
import subprocess
import traceback

from .adapter import Adapter

# Server Params
REMOTE_PORT = "tcp:7332"
REMOTE_SERVER = "localhost"

# Fuzz Actions
ACTION_FUZZ_INTENT = "vnd.dcsl.action.FUZZ_INTENT_START"
ACTION_FUZZ_NOTIF  = "vnd.dcsl.action.FUZZ_NOTIF_START"
ACTION_FUZZ_TEST   = "vnd.dcsl.action.FUZZ_TEST"

class Comando(Adapter):
    """
    A class to interact (via a socket connection) with the fuzzer installed
    on the target device.
    """

    def __init__(self, device=None):
        """
        initiate a socket connection between droidbot and the fuzzer app installed
        on the device
        """
        self.logger = logging.getLogger(self.__class__.__name__)
        self.host = REMOTE_SERVER
        if device is None:
            from droidbot.device import Device
            device = Device()
        self.device = device
        self.port = self.device.get_random_port()
        self.connected = False

        self.sock = None

    def connect(self):
        """
        establish socket connection
        :return:
        """
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            # forward host port to remote port
            serial_cmd = "" if self.device is None else "-s " + self.device.serial
            forward_cmd = "adb %s forward tcp:%d %s" % (serial_cmd, self.port, REMOTE_PORT)
            subprocess.check_call(forward_cmd.split())
            self.sock.connect((self.host, self.port))

        except socket.error:
            self.connected = False
            traceback.print_exc()

    def disconnect(self):
        """
        disconnect socket connection
        """
        self.connected = False
        if self.sock is not None:
            try:
                self.sock.close()
            except Exception as e:
                print(e)
        try:
            forward_remove_cmd = "adb -s %s forward --remove tcp:%d" % (self.device.serial, self.port)
            p = subprocess.Popen(forward_remove_cmd.split(), stderr=subprocess.PIPE, stdout=subprocess.PIPE)
            out, err = p.communicate()
        except Exception as e:
            print(e)
        self.__can_wait = False

    def send(self, msg):
        """
        sends a message to the server
        :param msg:
        :return:
        """
        try:
            data = json.dumps(msg) + '\r\n'
            self.sock.send (data.encode('utf-8'))

            # wait for response
            rtn = self.sock.recv(1024)
            if (rtn):
                return rtn.decode()

        except Exception as e:
            print(e)