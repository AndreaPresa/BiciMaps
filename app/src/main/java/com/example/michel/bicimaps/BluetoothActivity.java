package com.example.michel.bicimaps;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends Service {


    //UUID generada en uuiggenerator.net
    private static final UUID my_uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    //Direccion MAC del HC05
    private static final String MAC_ADDRESS = "00:14:03:06:08:CE";

    private static final int REQUEST_ENABLE_BT = 1;


    private BluetoothDevice[] pairedDevicesArray;
    private  Handler mBTHandler;
    private BroadcastReceiver mBroadcastReceiver;
    private BluetoothDevice mDevice;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private ConnectingThread mConnectingThread;
    private ConnectedThread mConnectedThread;

    private char order;
    private boolean stop_fan_Flag=false;

    private Context mContext;

    


    /*
        ConnectedThread mConnectedThread;
    */
    String TAG = "MapsActivity";
    StringBuilder messages;
    private boolean stopThread;
    private StringBuilder recDataString = new StringBuilder();
    final int handlerState = 1;//used to identify handler message
    private int Pm=0;




    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BT SERVICE", "SERVICE CREATED");
        stopThread = false;
        mContext = this;
        checkBTState();

    }

    @SuppressLint("HandlerLeak")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");

    if(intent!=null) {
        if (intent.getExtras() != null) {
            Bundle b = intent.getExtras();
            order = b.getChar("PM");

            if (order == 's') {
                stop_fan_Flag = true;

            }


                /*if(mConnectedThread == null){
                        checkBTState();

                }*/
            //Arranco los threads
    /*
                checkBTState();
    */
        }
    }
        if (mConnectedThread != null) {
            mConnectedThread.write(String.valueOf(order));

        }


        mBTHandler = new Handler() {

            /**
             * ESTO ES UN HANDLE MESSAGE ASI QUE ESTA ESPERANDO RECIBIR UN MENSAJE
             */
            public void handleMessage(android.os.Message msg) {
                Log.d("DEBUG", "handleMessage" + msg);
                //Aqui entra igualmente, porque msg.what siempre vale lo que se ponga en handlerState
                if (msg.what == handlerState) {           //if message is what we want
                    int decenaPM = 0;
                    int unidadPM = 0;
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    String recData = recDataString.toString();
                    int recData_length = recData.length();

                    if (stop_fan_Flag) {  //Caso de que haya que parar
                        if (recData != null && !recData.isEmpty()) {
                            for (int i = 0; i < recData_length; i++) {
                                if (recData.charAt(i) == 's') {
                                    mConnectedThread.write(String.valueOf(recData.charAt(i)));
                                    stop_fan_Flag=false;
                                }

                            }

                        }
                    }



                    if (recData.endsWith("\r\n")) {

                        if(recData.charAt(0) == 'r') {

                            String num = recData.substring(1, recData.length() - 2);
                            Log.d("RECEIVED_PM", num);
                            Pm = Integer.parseInt(num);

                            Log.d("RECORDED", recDataString.toString());
                            // Do stuff here with your data, like adding it to the database

                            Intent intent = new Intent("PM_Data");
                            intent.putExtra("TestData", Pm);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                        }

                        recDataString.delete(0, recDataString.length());                    //clear all string data
                    }

                    /*

                    if (recData != null && !recData.isEmpty()) {
                        for (int i = 0; i < recData_length; i++) {
                            if (Character.isDigit(recData.charAt(i))) {
                                if (recData_length - i != 1) { //Si tenemos dos numeros al final
                                    if (Character.isDigit(recData.charAt(i + 1))) {
                                        unidadPM = Character.getNumericValue(recData.charAt(i + 1));
                                        decenaPM = Character.getNumericValue(recData.charAt(i));
                                    }
                                } else unidadPM = Character.getNumericValue(recData.charAt(i));

                            }
                        }

                        Pm = decenaPM * 10 + unidadPM;

                    } else {
                        Pm = 0;
                    }

                        Log.d("RECORDED", recDataString.toString());
                        // Do stuff here with your data, like adding it to the database

                        Intent intent = new Intent("PM_Data");
                        intent.putExtra("TestData", Pm);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                   */

/*
                    disconnect();
*/
                }

            }


        };

        return super.onStartCommand(intent, flags, startId);


    }


    private void checkBTState() {

        if (mBluetoothAdapter == null) {
            Log.d("BT SERVICE", "BLUETOOTH NOT SUPPORTED BY DEVICE, STOPPING SERVICE");
            stopSelf();
        } else {
            if (mBluetoothAdapter.isEnabled()) {
                Log.d("DEBUG BT", "BT ENABLED! BT ADDRESS : " + mBluetoothAdapter.getAddress() + " , BT NAME : " + mBluetoothAdapter.getName());
                try {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MAC_ADDRESS);
                    Log.d("DEBUG BT", "ATTEMPTING TO CONNECT TO REMOTE DEVICE : " + MAC_ADDRESS);
                    mConnectingThread = new ConnectingThread(device);
                    mConnectingThread.start();
                } catch (IllegalArgumentException e) {
                    Log.d("DEBUG BT", "PROBLEM WITH MAC ADDRESS : " + e.toString());
                    Log.d("BT SEVICE", "ILLEGAL MAC ADDRESS, STOPPING SERVICE");
                    stopSelf();
                }
            } else {
                Log.d("BT SERVICE", "BLUETOOTH NOT ON, STOPPING SERVICE");
                stopSelf();
            }
        }
    }



    private class ConnectingThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectingThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = mmDevice.createRfcommSocketToServiceRecord(my_uuid);
            } catch (IOException e) {
                Log.d("RFCOMM BT", "FAIL CREATING RFCOMM SOCKET");

            }
            mmSocket = tmp;
        }

        @Override
        public void run() {
            super.run();
            Log.d("DEBUG BT", "IN CONNECTING THREAD RUN");
            // Establish the Bluetooth socket connection.
            // Cancelling discovery as it may slow down connection
            mBluetoothAdapter.cancelDiscovery();
            BluetoothSocket tmp = null;

            try {
                mmSocket.connect();
                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();

                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
                //I send a character when resuming.beginning transmission to check device is connected
                //If it is not an exception will be thrown in the write method and finish() will be called

            } catch (IOException e) {
                try {
                    Log.d("DEBUG BT", "SOCKET CONNECTION FAILED : " + e.toString());
                    Log.d("BT SERVICE", "SOCKET CONNECTION FAILED, STOPPING SERVICE");
                    mmSocket.close();
                    stopSelf();

                } catch (IOException e2) {
                    Log.d("DEBUG BT", "SOCKET CLOSING FAILED :" + e2.toString());
                    Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                    stopSelf();
                    //insert code to deal with this
                }
            } catch (IllegalStateException e) {
                Log.d("DEBUG BT", "CONNECTED THREAD START FAILED : " + e.toString());
                Log.d("BT SERVICE", "CONNECTED THREAD START FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }

        public void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                mmSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
                Log.d("DEBUG BT", e2.toString());
                Log.d("BT SERVICE", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }


        // New Class for Connected Thread
        private class ConnectedThread extends Thread {
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            //creation of the connect thread
            public ConnectedThread(BluetoothSocket socket) {
                Log.d("DEBUG BT", "IN CONNECTED THREAD");
                InputStream tmpIn = null;
                OutputStream tmpOut = null;

                try {
                    //Create I/O streams for connection
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                    Log.d("DEBUG BT", e.toString());
                    Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                    stopSelf();
                }

                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                Log.d("DEBUG BT", "IN CONNECTED THREAD RUN");
                byte[] buffer = new byte[256];
                int bytes;

                // Keep looping to listen for received messages
                while (true && !stopThread) {
                    try {
                        bytes = mmInStream.read(buffer);            //read bytes from input buffer
                        String readMessage = new String(buffer, 0, bytes);
                        Log.d("DEBUG BT PART", "CONNECTED THREAD " + readMessage);
                        // Send the obtained bytes to the UI Activity via handler

                        mBTHandler.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    } catch (IOException e) {
                        Log.d("DEBUG BT", e.toString());
                        Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                        stopSelf();
                        break;
                    }
                }
            }
            //write method
            public void write(String input) {
                byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
                try {
                    mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
                } catch (IOException e) {
                    //if you cannot write, close the application
                    Log.d("DEBUG BT", "UNABLE TO READ/WRITE " + e.toString());
                    Log.d("BT SERVICE", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                    stopSelf();
                }
            }

            public void closeStreams() {
                try {
                    //Don't leave Bluetooth sockets open when leaving activity
                    mmInStream.close();
                    mmOutStream.close();
                } catch (IOException e2) {
                    //insert code to deal with this
                    Log.d("DEBUG BT", e2.toString());
                    Log.d("BT SERVICE", "STREAM CLOSING FAILED, STOPPING SERVICE");
                    stopSelf();
                }
            }
        }


     public void disconnect(){
         mBTHandler.removeCallbacksAndMessages(null);
         stopThread = true;
         if (mConnectedThread != null) {
             mConnectedThread.closeStreams();
             mConnectedThread=null;
         }
         if (mConnectingThread != null) {
             mConnectingThread.closeSocket();
             mConnectingThread=null;
         }


     }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBTHandler.removeCallbacksAndMessages(null);
        stopThread = true;
        if (mConnectedThread != null) {
            mConnectedThread.closeStreams();
        }
        if (mConnectingThread != null) {
            mConnectingThread.closeSocket();
        }
        Log.d("SERVICE", "onDestroy");

/*
        unregisterReceiver(mBroadcastReceiver);
*/

        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
       /* if(intent.getExtras()!=null) {
            Bundle b = intent.getExtras();
            order = b.getChar("PM");
        }*/
        return null;

    }







}
