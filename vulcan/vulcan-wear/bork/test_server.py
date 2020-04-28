import logging
import socket
import time
import sys
import json
import subprocess
import traceback
import struct

CLIENT_APP_REMOTE_ADDR = "tcp:7332"
CLIENT_APP_PACKAGE = "io.github.ylimit.droidbotapp"

DEFAULT_SERIAL = "TKQ7N18711000107"

class Test(object):

    def __init__(self,
                device_serial,
                package,
                strategy,
                skip):


        self.device_serial = device_serial # change to self.device.serial
        self.cmd = "-s %s" % self.device_serial
        self.port = 5460
        self.host = "localhost"

        self.package = package
        self.strategy = strategy
        self.skip = skip


        if self.strategy == "stop":
            print('\eba\ trying to stop %s process' % self.package)
            self.stop_process()
            print('\eba\ done')
        else:
            print('\eba\ starting...')
            self.connect()
            print('\eba\ test_server.py: connected')
            for i in range(0,1):
                self.send("OK --%d--" % i)
            time.sleep(2)
            print('\eba\ done!')
            self.disconnect()
            print('\eba\ test_server.py: disconnected')


    # def start(self):
    #     # Create a TCP/IP socket
    #     self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    #     # Bind the socket to the port
    #     self.sock.bind((self.host, self.port))

    def connect(self):
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        try:
            # forward host port to remote port
            # serial_cmd = "" if self.device is None else "-s " + self.device.serial
            serial_cmd = ""

            forward_cmd = "adb %s forward tcp:%d %s" % (self.cmd, self.port, CLIENT_APP_REMOTE_ADDR)
            print("\eba\ droidbot_app.py connect(): {}".format(forward_cmd))
            subprocess.check_call(forward_cmd.split())
            self.sock.connect((self.host, self.port))

            # self.sock.bind((self.host, 7330))
            # self.sock.connect((self.host, 7330))

            # import threading
            # listen_thread = threading.Thread(target=self.listen_messages)
            # listen_thread.start()
        except socket.error:
            self.connected = False
            traceback.print_exc()

    def disconnect(self):
        """
        disconnect telnet
        """
        self.connected = False
        if self.sock is not None:
            try:
                self.sock.close()
            except Exception as e:
                print(e)
        try:
            forward_remove_cmd = "adb -s %s forward --remove tcp:%d" % (self.device_serial, self.port)
            p = subprocess.Popen(forward_remove_cmd.split(), stderr=subprocess.PIPE, stdout=subprocess.PIPE)
            out, err = p.communicate()
        except Exception as e:
            print(e)
        self.__can_wait = False


    def stop_process(self):
        try:
            forward_remove_cmd = "adb -s %s shell am force-stop %s" % (self.device_serial, self.package)
            p = subprocess.Popen(forward_remove_cmd.split(), stderr=subprocess.PIPE, stdout=subprocess.PIPE)
        except Exception as e:
            print(e)

    def send(self, msg):
        # msg = msg + '\r\n';
        # print('\eba\ sending: {}'.format(msg))
        # self.sock.send(input(msg))
        # self.sock.send(bytes(msg, 'utf-8'))

        dict = {
            'target': self.package,
            'action': 'vnd.dcsl.action.FUZZ_INTENT_START',
            'strategy': 'strategy/' + self.strategy,
            'skip': self.skip
        }
        print('\eba\ sending: {}'.format(dict))

        # print ('==> %d' % self.sock.send(bytes(msg, 'utf-8')))
        data = json.dumps(dict) + '\r\n'
        print ('==> %d' % self.sock.send(data.encode('utf-8')))

        rtn = self.sock.recv(1024)
        print ("%s <== " % rtn)
        self.decode(rtn)
        time.sleep(0.5)

    def decode(self, data):
        # dato = struct.unpack(">BBI", data)
        print (data.decode())
