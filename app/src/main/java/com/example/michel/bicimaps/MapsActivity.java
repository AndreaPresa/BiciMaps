package com.example.michel.bicimaps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;



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
    double latitude,longitude;
    private boolean LP_flag=false;
    private boolean FB_flag = false;

    /**Los modos de experimento no están implementados.*/

    private Spinner mapsOptions;
    private Spinner mode;
    private Spinner period;

    private int loc_request_time;

    private String tipos_mapa[] = {"Normal", "Satélite", "Híbrido"};
    private String modos[] = {"Manual", "Auto"};
    private Long periodos[] = {(long) 5, (long) 10, (long) 20};
    private FloatingActionButton locButton;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mContext = this;


        /** Simulación */
        /**Ver con Yago porque la variable FB_flag cambia de valor aparentemente sola...*/

        Intent intent = getIntent();
        if (intent.getExtras()!=null) {
            FB_flag = intent.getExtras().getBoolean("FBflag");
        }



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
                save_location(lastlocation);
                mHandler.postDelayed(this, Long.parseLong(String.valueOf(period.getSelectedItem()))*1000);
            }
        }, Long.parseLong(String.valueOf(period.getSelectedItem()))*1000);

        //Boton inicio exp
        locButton = (FloatingActionButton) findViewById(R.id.btn_inicioExp);
        locButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Bandera que permite la escritura automatica en Firebase
                FB_flag=true;

            }
        });

        /** Localización y GoogleMap*/

        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Daba problemas de nullpointer reference asi que le ponemos la condicion
        if(mapFragment != null){
            mapFragment.getMapAsync(this);
        }

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
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this,"El dispositivo no es válido para esta app", Toast.LENGTH_LONG).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext, R.style.MyDialogTheme);
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
            alert.show();


        }

    }



    private void isLocationEnabled() {

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext, R.style.MyDialogTheme);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your locations setting is not enabled. Please enable it in settings menu.");
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
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





















}