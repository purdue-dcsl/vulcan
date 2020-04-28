package edu.purdue.dagobah.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

import edu.purdue.dagobah.common.Constants;
import edu.purdue.dagobah.common.FuzzAction;
import edu.purdue.dagobah.common.FuzzCommand;
import edu.purdue.dagobah.fuzzer.NotifFuzzer;
import edu.purdue.dagobah.fuzzer.TestFuzzer;

public class SocketServerManager extends Service {

    private static final String TAG = "Kylo/SServer";
    private static final String REPLY = "DONE";
    private ServerSocket server;
    private Socket socket;
    private Handler handler;
    private LiveThread listeningThread;
    private String line;


    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    /**
     * Create an instance of {@link SocketServerManager}.
     */
    public SocketServerManager() {
    }

    /* ---------------------------------------------------------------------------
     * service binder helpers / classes
     * @see: https://developer.android.com/guide/components/bound-services
     * --------------------------------------------------------------------------- */

    /**
     * Class used for the client Binder for {@link FuzzerManager}.
     * Because we know this service always runs in the same processs as it clients, we don't
     * need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SocketServerManager getService() {
            // Return this instance of StatefulFuzzer so clients can call public methods
            return SocketServerManager.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* ---------------------------------------------------------------------------
     * communication primitives
     * --------------------------------------------------------------------------- */

    /**
     * Create a {@link ServerSocket} instance to accept connection from a client. The client
     * is usually a python script that will send commands to the server.
     */
    public void connect() {

        // start ServerSocket
        if ( server == null || server.isClosed() ) {
            try {
                Log.d(TAG, String.format("Starting socket server on port: %d",
                        Constants.SERVER_PORT));
                server = new ServerSocket(Constants.SERVER_PORT);
                handler = new Handler();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // start listener
        if ( listeningThread == null ) {
            listeningThread = new LiveThread();
        }

        if ( listeningThread != null ) {
            Log.d(TAG, String.format("Starting listener"));
            listeningThread.start();
        }

    }

    /**
     * Close the {@link ServerSocket} instance.
     */
    public void disconnect() {
        listeningThread.accepting = false;

        try {
            Log.d(TAG, "Closing socket");
            socket.close();
        } catch (IOException ex) {
            Log.e(TAG, Log.getStackTraceString(ex));
        }

        if ( server != null ) {
            try {
                server.close();
            } catch (IOException ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            }

        }
    }


    /**
     * Live listener to message from orchestrator.
     */
    private class LiveThread extends Thread {

        boolean accepting = false;

        @Override
        public void run() {

            try {

                accepting = true;
                Log.d(TAG, String.format("Start to accept socket clients on port {%d}",
                        Constants.SERVER_PORT));
                while (accepting) {
                    socket = server.accept();
                    Log.d(TAG, "Client accepted!");
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));

                    // read received data
                    while ((line = in.readLine()) != null) {

                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                try {
                                    FuzzCommand cmd = new FuzzCommand(new JSONObject(line));
                                    Log.e(TAG, cmd.toString());

                                    switch(cmd.getAction()) {

                                        case ACTION_FUZZ_INTENT_START:
                                            doFuzzIntent(cmd);
                                            sendResponse(REPLY);
                                            break;

                                        case ACTION_FUZZ_NOTIF_START:
                                            doFuzzNotif(cmd);
                                            sendResponse(REPLY);
                                            break;

                                        case ACTION_FUZZ_TEST:
                                            doFuzzTest(cmd);
                                            sendResponse(REPLY);
                                            break;

                                        case ACTION_UNKNOWN:
                                        default:
                                            sendResponse(REPLY);
                                            break;
                                    }


                                } catch (JSONException ex) {
                                    Log.e(TAG, Log.getStackTraceString(ex));
                                } /*catch (IOException ex) {
                                    Log.e(TAG, Log.getStackTraceString(ex));
                                }*/

                                Log.d(TAG, line);
                            }
                        });
                        Log.d(TAG, String.format(" => %s", line));
                    }
                    if (line == null)
                        Log.d(TAG, "No more data");
                }
            } catch (IOException ex) {
                Log.e(TAG, Log.getStackTraceString(ex));
            } finally {
                disconnect();
            }

        }
    }

    /**
     * Responder thread.
     * Send message to the orchestrator.
     */
    private  class ResponderThread extends Thread {
        String[] messages;

        ResponderThread(String... messages) {
            this.messages = messages;
        }

        public void run() {
            for (String message : this.messages) {
                byte[] bytes  = message.getBytes();
                synchronized (socket) {
                    try {
                        socket.getOutputStream().write(bytes);
                    } catch (IOException ex) {
                        Log.e(TAG, Log.getStackTraceString(ex));
                    }
                }
            }
        }

    }

    /**
     * Sends a message to the orchestrator.
     * @param messages the message
     */
    private void sendResponse(String... messages) {
        new ResponderThread(messages).start();
    }

    /* ---------------------------------------------------------------------------
     * Fuzzing methods
     * This methods need to be implemented (override)
     * --------------------------------------------------------------------------- */

    public void doFuzzIntent(FuzzCommand cmd) {
        Log.e(TAG, "Intent Fuzzer: Not implemented");
    }

    public void doFuzzNotif(FuzzCommand cmd){
        Log.e(TAG, "Notification Fuzzer: Not implemented");
    }

    public void doFuzzTest(FuzzCommand cmd){
        Log.e(TAG, "Test Fuzzer: Not implemented");
    }

}
