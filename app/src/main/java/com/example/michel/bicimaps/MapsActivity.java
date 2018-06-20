package com.example.michel.bicimaps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**He implementado en esta actividad una bandera (FB_flag) para parar las actualizaciones.
Habría que comprobar si el gps está habilitado y si no pedirle al usuario que lo habilitara*/


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    LocationManager locationManager;
    Context mContext;

    private GoogleMap mMap;
    private GoogleApiClient client;
    private Location lastlocation;

    public static final int REQUEST_LOCATION_CODE = 99;
    double latitude, longitude;
    private boolean LP_flag = false;
    private boolean FB_flag;

    /**
     * Los modos de experimento no están implementados.
     */

    private Spinner mapsOptions;
    private Spinner mode;
    private Spinner period;

    private int loc_request_time;

    private String tipos_mapa[] = {"Normal", "Satélite", "Híbrido"};
    private String modos[] = {"Manual", "Auto"};
    private Long periodos[] = {(long) 5, (long) 10, (long) 20};
    private FloatingActionButton locButton;



    //UUID generada en uuiggenerator.net
    private static final UUID uuid = UUID.fromString("28b88383-4770-46b8-ac20-9f1d0dff17c7");
    private static final int REQUEST_ENABLE_BT = 1;

    EditText send_data;
    TextView view_data;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice mDevice;
    private BluetoothDevice []  pairedDevicesArray;
    private Handler mHandler;
/*
    ConnectedThread mConnectedThread;
*/
    String TAG = "MapsActivity";
    StringBuilder messages;
    private UUID deviceUUID;
/*
    //Pairing process
    public void pairDevice(View v) {

        //Comprobamos que el dispositivo soporta la tecnología bluetooth
        if(bluetoothAdapter!=null){
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();



        Log.e("MapsActivity", "" + pairedDevices.size());
        if (pairedDevices.size() > 0) {
            Object[] devices = pairedDevices.toArray();
            BluetoothDevice device = (BluetoothDevice) devices[0];
            //ParcelUuid[] uuid = device.getUuids();
            Log.e("MapsActivity", "" + device);
            //Log.e("MapsActivity", "" + uuid)

            ConnectThread connect = new ConnectThread(device, MY_UUID_INSECURE);
            connect.start();
        }



        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN mConnectThread ");

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRfcommSocket using UUID: "
                        + MY_UUID_INSECURE);
                tmp = mmDevice.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "ConnectThread: Could not create InsecureRfcommSocket " + e.getMessage());
            }

            mmSocket = tmp;

            // Make a connection to the BluetoothSocket

            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + MY_UUID_INSECURE);
            }

            //will talk about this in the 3rd video
            connected(mmSocket);
        }

        public void cancel() {
            try {
                Log.d(TAG, "cancel: Closing Client Socket.");
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "cancel: close() of mmSocket in Connectthread failed. " + e.getMessage());
            }
        }
    }

    private void connected(BluetoothSocket mmSocket) {
        Log.d(TAG, "connected: Starting.");

        // Start the thread to manage the connection and perform transmissions
        ConnectedThread mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream

            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmInStream.read(buffer);
                    final String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            view_data.setText(incomingMessage);
                        }
                    });


                } catch (IOException e) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.getMessage());
                    break;
                }
            }
        }


        public void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to outputstream: " + text);
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "write: Error writing to output stream. " + e.getMessage());
            }
        }

        *//* Call this from the main activity to shutdown the connection *//*
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }


    public void SendMessage(View v) {
        byte[] bytes = send_data.getText().toString().getBytes(Charset.defaultCharset());
        mConnectedThread.write(bytes);
    }




    public void Start_Server (View view){

        AcceptThread accept = new AcceptThread();
        accept.start();

    }

    private class AcceptThread extends Thread {

        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket
            // because mmServerSocket is final.
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code.
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("HC05", MY_UUID_INSECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket's listen() method failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned.
            while (true) {
                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's accept() method failed", e);
                    break;
                }

                if (socket != null) {
                    // A connection was accepted. Perform work associated with
                    // the connection in a separate thread.
*//*
                    manageMyConnectedSocket(socket);
*//*
                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Socket's close() method failed", e);
                    }
                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
    }






*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext= MapsActivity.this;

        FB_flag = false;


/*
         view_data = findViewById(R.id.view_Bluetooth);
           pairDevice(view_data);

        bluetoothManager mBluetoothManager = new bluetoothManager();



        send_data = (EditText) findViewById(R.id.editText);
        view_data = (TextView) findViewById(R.id.textView);

         if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

*/




        /** Simulación
        Ver con Yago porque la variable FB_flag cambia de valor aparentemente sola...*/


        //Parte de arriba del Maps Layout
        mapsOptions = (Spinner) findViewById(R.id.cmbMaptype);
        mode = (Spinner) findViewById(R.id.cmbMode);
        period = (Spinner) findViewById(R.id.cmbPeriod);


        //Funcion que crea los spinners
        spinners();

        //Handler que hace de timer para temporizar los experimentos
        final Handler mHandler = new Handler();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mHandler.postDelayed(this, Long.parseLong(String.valueOf(period.getSelectedItem()))*1000);
                save_location(lastlocation);
            }
        }, Long.parseLong(String.valueOf(period.getSelectedItem()))*1000);

        //Boton inicio experimentos
        locButton = (FloatingActionButton) findViewById(R.id.btn_inicioExp);
        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Bandera que permite la escritura automatica en Firebase
                FB_flag = true;

            }
        });

        /** Localización y GoogleMap*/



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Daba problemas de nullpointer reference asi que le ponemos la condicion
        if(mapFragment != null){
            mapFragment.getMapAsync(this);
        }

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);


        //Control de permisos de ubicación
        permissions_control();

        //Petición activar Bluetooth
        isBluetoothEnabled();


    }









    /** SIMULACIÓN */




    public void spinners(){

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, R.layout.spinner, tipos_mapa);
        adapter.setDropDownViewResource(R.layout.spinner);
        mapsOptions.setAdapter(adapter);

        ArrayAdapter<String> adapter1 =
                new ArrayAdapter<>(this, R.layout.spinner, modos);
        adapter1.setDropDownViewResource(R.layout.spinner);
        mode.setAdapter(adapter1);

        ArrayAdapter<Long> adapter2 =
                new ArrayAdapter<>(this, R.layout.spinner, periodos);
        period.setAdapter(adapter2);
        adapter2.setDropDownViewResource(R.layout.spinner);




        //Funcion asignacion tipo de mapa
        mapsOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView_maps, View view, int i, long l) {
                long id = adapterView_maps.getItemIdAtPosition(i);

                //Obtenemos el indice de posicion del elemento seleccionado y asignamos un mapa en funcion de dicho indice
                if (id== 0) {

                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }

                else if (id== 1) {

                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }

                else if (id== 2) {

                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                }

            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView_maps) {

                //El tipo de mapa es normal por defecto
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });



    }




    /** PERMISOS Y HABILITACIONES */




    public void permissions_control(){

        //Control de Permiso de Localización
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_CODE);

        }
        //Si ya está aceptado el permiso de localización, se le pide al usuario que active la
        //localización, en el caso de no tenerla activada

        else { isLocationEnabled(); }





    }


    public void isBluetoothEnabled(){

        if (bluetoothAdapter == null) {
            Toast.makeText(this,"El dispositivo no es válido para esta app", Toast.LENGTH_LONG).show();
        }

        if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


            /*AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext, R.style.MyDialogTheme);
            alertDialog.setTitle("Enable Bluetooth");
            alertDialog.setMessage("Your Bluetooth is not enabled. Please enable it in the settings menu.");
            alertDialog.setPositiveButton("Bluetooth Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();*/


        }


    }

        //Función que controla la respuesta a la activación del Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                bluetoothAdapter.enable();
                Toast.makeText(mContext, "Bluetooth enabled correctly", Toast.LENGTH_SHORT).show();

                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
            }
            else {
                Toast.makeText(mContext, "Error while enabling Bluetooth. Try again", Toast.LENGTH_SHORT).show();

            }
        }
    }



    private void isLocationEnabled() {

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){

            AlertDialog.Builder alertDialog=new AlertDialog.Builder(MapsActivity.this, R.style.Theme_AppCompat);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your location is not enabled. Please enable it in settings menu.");
            alertDialog.setPositiveButton("Location Settings", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    Intent intent=new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            AlertDialog alert=alertDialog.create();
            alert.show();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch(requestCode)

        {
            case REQUEST_LOCATION_CODE:

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    isLocationEnabled();

                else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.MyDialogTheme);
                    builder.setMessage("Para el correcto funcionamiento de la aplicación, debes aceptar el permiso de ubicación.")
                            .setTitle("Información importante")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                    builder.show();

                    if (!LP_flag) {
                        ActivityCompat.requestPermissions(this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_LOCATION_CODE);
                        LP_flag = true;
                    }

                }


        }
    }




        /** LOCALIZACIÓN */



    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastlocation=location;
            latitude = location.getLatitude();
            longitude = location.getLongitude();

            /*if(currentLocationmMarker != null) {
                currentLocationmMarker.remove();
            }*/

            Log.d("lat = ",""+latitude);
            LatLng latLng = new LatLng(latitude , longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));


        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }


    };

    //Esta funcion se encarga de recoger la localizacion de OnLocationChanged y temporizar su escritura en la base de datos.
    //Esto lo hace esperando a que se determinen las condiciones del experimento y una vez hecho esto, genera automaticamente los registros

    private void save_location (Location location){
        lastlocation  = location;

        if (FB_flag) {
            if (lastlocation == null){
                Toast.makeText(getApplicationContext(), "Localizacion nula", Toast.LENGTH_SHORT).show();

            }
            else {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Bundle b = new Bundle();
                b.putDouble("latitud", latitude);
                b.putDouble("longitud", longitude);
                Intent i = new Intent(MapsActivity.this, FireBaseActivity.class);
                i.putExtra("bundleFire", b);
                startActivity(i);
            }

        }

    }




    /** GOOGLEMAP */




     // Manipulates the map once available.
     // This callback is triggered when the map is ready to be used.
     // This is where we can add markers or lines, add listeners or move the camera. In this case,
     // we just add a marker near Sydney, Australia.
     // If Google Play services is not installed on the device, the user will be prompted to install
     // it inside the SupportMapFragment. This method will only be triggered once the user has
     // installed Google Play services and returned to the app.


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }


    }


    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        client.connect();

    }





    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Situamos la camara en Madrid

        LatLng madrid = new LatLng(40.4167754,-3.7037901999999576);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid,13));

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER,
                    1000,
                    5,
                    locationListenerGPS);

        }
    }



    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    protected void onResume(){

        super.onResume();
        FB_flag=false;

    }

    @Override
    protected void onPause(){

        super.onPause();
        //Paramos el proceso de apuntar si la actividad queda en segundo plano



    }

















}