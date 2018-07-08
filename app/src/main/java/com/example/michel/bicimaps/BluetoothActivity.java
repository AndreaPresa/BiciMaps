package com.example.michel.bicimaps;

import android.annotation.SuppressLint;
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

    private Context mContext;




    /*
        ConnectedThread mConnectedThread;
    */
    String TAG = "MapsActivity";
    StringBuilder messages;
    private boolean stopThread;
    private StringBuilder recDataString = new StringBuilder();
    final int handlerState = 1;//used to identify handler message
    private int Pm;




    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BT SERVICE", "SERVICE CREATED");
        stopThread = false;
        mContext = this;

    }

    @SuppressLint("HandlerLeak")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BT SERVICE", "SERVICE STARTED");
        mBTHandler = new Handler() {

            /**  ESTO ES UN HANDLE MESSAGE ASI QUE ESTA ESPERANDO RECIBIR UN MENSAJE, POR ESO NO ENTRA */
            public void handleMessage(android.os.Message msg) {
                Log.d("DEBUG", "handleMessage");
                //Aqui entra igualmente, porque msg.what siempre vale lo que se ponga en handlerState
                if (msg.what == handlerState) {                                     //if message is what we want
                    String readMessage = (String) msg.obj;

                    // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);
                    String recData = recDataString.toString();
                    //Me daba error porque no se puede convertir la cadena que envia
                    //el modulo " " a integer.. encontre esa regular expression que funciona
                    if(recData.matches("\\d+(?:\\.\\d+)?")){
                        Pm = Integer.parseInt(recData.substring(0, 1));
                    }
                    else Pm=0;
                    Log.d("RECORDED", recDataString.toString());
                    // Do stuff here with your data, like adding it to the database

                    Intent intent = new Intent("PM_Data");
                    intent.putExtra("TestData", Pm);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                }
                recDataString.delete(0, recDataString.length());                    //clear all string data
            }


      };

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
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
                tmp = device.createRfcommSocketToServiceRecord(my_uuid);
            } catch (IOException e) {
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
            try {
                mmSocket.connect();
                Log.d("DEBUG BT", "BT SOCKET CONNECTED");
                mConnectedThread = new ConnectedThread(mmSocket);
                mConnectedThread.start();
                Log.d("DEBUG BT", "CONNECTED THREAD STARTED");
                //I send a character when resuming.beginning transmission to check device is connected
                //If it is not an exception will be thrown in the write method and finish() will be called
                mConnectedThread.write("1");
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
        unregisterReceiver(mBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

           /* if(mmSocket.isConnected()){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"HC05 connected succesfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);
        }

        *//** Will cancel an in-progress connection, and close the socket *//*
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
*/
   /* private class CommunicationThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public CommunicationThread (BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        *//* Call this from the main activity to send data to the remote device *//*
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        *//* Call this from the main activity to shutdown the connection *//*
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

    }*/

   /* public void manageConnectedSocket(BluetoothSocket mBluetoothSocket){


    }



    public void btAdapters() {

        final BTDevicesAdapter mBTDevicesAdapter = new BTDevicesAdapter(BluetoothActivity.this, names, addresses);


// Create a BroadcastReceiver for ACTION_FOUND
        mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    names.add(device.getName());
                    addresses.add(device.getAddress());
                    mBTDevicesAdapter.notifyDataSetChanged();


                }
            }
        };
// Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, filter); // Don't forget to unregister during onDestroy



        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            names.add(device.getName());
            addresses.add(device.getAddress());
        }

        mListView.setAdapter(mBTDevicesAdapter);


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = (String) mListView.getItemAtPosition(position);
                 mConnectingThread = new ConnectingThread(mBluetoothAdapter.getRemoteDevice(value));
                mConnectingThread.start();



            }
        });

    }

    public void isBluetoothEnabled() {

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no es válido para esta app", Toast.LENGTH_LONG).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        } else if (mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
            btAdapters();

        }


    }

    //Función que controla la respuesta a la activación del Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mBluetoothAdapter.enable();

                Toast.makeText(BluetoothActivity.this, "Bluetooth enabled correctly", Toast.LENGTH_SHORT).show();
                btAdapters();

            } else {
                Toast.makeText(BluetoothActivity.this, "Error while enabling Bluetooth. Try again", Toast.LENGTH_SHORT).show();

            }
        }
    }
*/




}
