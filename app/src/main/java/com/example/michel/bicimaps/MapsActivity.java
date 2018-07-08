package com.example.michel.bicimaps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.location.Criteria;
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
import android.support.v4.content.LocalBroadcastManager;
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
import java.util.Arrays;
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

    private String provider_NETWORK;
    private String provider_GPS;

    private int PM_FB_counter;
    private static final int PMDefault = 0;
    private int PMData;
    private int [] PMData_array;
    private int PMData_counter;
    private final int PMData_max = 10;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext= MapsActivity.this;

        FB_flag = false;


        Intent bindIntent = new Intent(this, BluetoothActivity.class);
        startService(bindIntent);

        //Inicializo PMDataCounter
        PMData_counter=0;

        //Declaro el vector de enteros y lo inicializo
        PMData_array = new int[PMData_max];
        Arrays.fill(PMData_array, 0);
        //Inicializo PM_FB_Counter
        PM_FB_counter=0;
        //Registro el Broadcast para recibir el dato de PM

        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                        new IntentFilter("PM_Data"));

        /** Simulación */


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
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));


        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        public void onProviderEnabled(String provider) {

        }

        public void onProviderDisabled(String provider) {

        }


    };

    LocationListener locationListenerNetwork = new LocationListener() {
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
                Toast.makeText(getApplicationContext(), "Localización nula", Toast.LENGTH_SHORT).show();
                changeProvider();

            }
            else {


                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Bundle b = new Bundle();
                b.putDouble("latitud", latitude);
                b.putDouble("longitud", longitude);
                b.putInt("pm", PMData_array[PM_FB_counter]);
                if(PM_FB_counter==PMData_max-1) {
                    PM_FB_counter = 0;
                }
                else {
                    PM_FB_counter++;
                }
                Intent i = new Intent(MapsActivity.this, FireBaseActivity.class);
                i.putExtra("bundleFire", b);
                startActivity(i);
            }

        }

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
             PMData = intent.getIntExtra("TestData", PMDefault);
             PMData_array[PMData_counter]=PMData;
             //Cuando llega a 10 elementos, sobreeescribo el vector
            if(PMData_counter==PMData_max-1) {
                PMData_counter = 0;
            }
            else {
                PMData_counter++;
            }

        }


    };




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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madrid,12));

        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED) {
            //Cambio el GPS provider por NETWORK Provider para no depender de satelites.

                locationManager.requestLocationUpdates(locationManager.GPS_PROVIDER,
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


    public void changeProvider (){

        Criteria crit = new Criteria();
        crit.setPowerRequirement(Criteria.POWER_LOW);
        crit.setAccuracy(Criteria.ACCURACY_COARSE);
        provider_NETWORK = locationManager.getBestProvider(crit, true);

        Criteria crit2 = new Criteria();
        crit2.setAccuracy(Criteria.ACCURACY_FINE);
        provider_GPS = locationManager.getBestProvider(crit2, true);

        if (locationManager.isProviderEnabled(provider_GPS)){
            locationManager.getBestProvider(crit, false);

        }
        else if (locationManager.isProviderEnabled(provider_NETWORK)){
            locationManager.getBestProvider(crit2,false);


        }
    }















}