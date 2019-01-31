package com.example.michel.bicimaps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Criteria;
import android.provider.Settings;
import android.renderscript.ScriptGroup;
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
import android.widget.CalendarView;
import android.widget.DatePicker;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


/**He implementado en esta actividad una bandera (FB_flag) para parar las actualizaciones.
Habría que comprobar si el gps está habilitado y si no pedirle al usuario que lo habilitara*/


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "MapStyleError";
    LocationManager locationManager;
    Context mContext;

    private GoogleMap mMap;
    private GoogleApiClient client;
    private Location lastlocation;

    public static final int REQUEST_LOCATION_CODE = 99;
    double latitude, longitude;
    private boolean LP_flag = false;
    private boolean FB_flag;


    private Spinner mapsOptions;
    private Spinner mode;
    private Spinner period;

    private ArrayAdapter<String> adapter_maps;
    private ArrayAdapter<String> adapter_mode;
    private ArrayAdapter<Long> adapter_time;


    private String dateSpinner="Histórico";

    private String tipos_mapa[] = {"Normal", "Satélite", "Híbrido"};
    private String modos[] = {"Firebase", "RealTime", dateSpinner};
    private ArrayList<String> modos_lst;
    private Long periodos[] = {(long) 2, (long) 5, (long) 10};
    private FloatingActionButton locButton;

    private FloatingActionButton calButton;
    private Calendar myCalendar;
    private EditText dateET;
    private String today;
    private SimpleDateFormat sdf;

    private FloatingActionButton fbButton;
    private boolean realTime_flag=false;

    private boolean mMap_locationFlag = true;
    private boolean mMap_erase_Flag = false;
    private FloatingActionButton location_onButton;
    private FloatingActionButton readButton;
    private DatabaseReference dbLocations=null;
    private DatabaseReference dbLocations1=null;
    List<WeightedLatLng> latLngList=null;

    private TileOverlay mOverlay;

    private String provider_NETWORK;
    private String provider_GPS;

    private int PM_FB_counter;
    private static final int PMDefault = 0;
    private int PMData;
    private int [] PMData_array;
    private int PMData_counter;
    private final int PMData_max = 10;

    private char start_PM='p';
    private char read_PM = 'r';
    private char finish_PM='s';
    private boolean PM_flag=false;

    private boolean save_loc_PM_flag=false;
    private boolean dBhaschild=false;



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
        Arrays.fill(PMData_array, -1);
        //Inicializo PM_FB_Counter
        PM_FB_counter=0;
        //Registro el Broadcast para recibir el dato de PM
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mMessageReceiver,
                        new IntentFilter("PM_Data"));


        /** SIMULACIÓN */
        //Calendario
        myCalendar= Calendar.getInstance();
        String myFormat = "dd-MM-yyyy";
        sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
        today = sdf.format(myCalendar.getTime());

        latLngList = new ArrayList<>();


        //Parte de arriba del Maps Layout
        mapsOptions = findViewById(R.id.cmbMaptype);
        mode = findViewById(R.id.cmbMode);
        period = findViewById(R.id.cmbPeriod);

        modos_lst = new ArrayList<>(Arrays.asList(modos));


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


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };

        /**FLOATING ACTION BUTTONS */

        //Boton mirar FB solo habilitado si se pulsa en Historico en el spinner

        fbButton = findViewById(R.id.btnFB);
        fbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Cuando enviemos el intent desde aqui, solo pretendemos ver lo que hay en FB
                //Mandamos tambien una bandera
                Intent intent = new Intent(MapsActivity.this,
                        FireBaseActivity.class);
                Bundle b = new Bundle();
                b.putString("date", dateSpinner);
                intent.putExtra("onlyRead",b);
                startActivity(intent);
            }
        });


        //Boton calendario solo habilitado si se pulsa en historico en el spinner
        calButton = findViewById(R.id.btn_calendar);
        calButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new DatePickerDialog(MapsActivity.this, R.style.MyDialogTheme,
                        date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        //Boton eliminar localizacion en Mapa
        location_onButton = findViewById(R.id.btn_Locs);
        location_onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    //Cambio el valor para que se muestre o no

                    if (mMap_locationFlag) {
                        mMap_locationFlag = !mMap_locationFlag;
                        int id = getResources().getIdentifier("ic_location_off",
                                "drawable", "com.example.michel.bicimaps");
                        location_onButton.setImageResource(id);
                        mMap.setMyLocationEnabled(false);


                    } else if (!mMap_locationFlag) {
                        mMap_locationFlag = !mMap_locationFlag;
                        int id = getResources().getIdentifier("ic_location_on",
                                "drawable", "com.example.michel.bicimaps");
                        location_onButton.setImageResource(id);
                        mMap.setMyLocationEnabled(true);

                    }

                }
            }
        });


        //Boton inicio experimentos
        locButton = findViewById(R.id.btn_inicioExp);
        locButton.setOnClickListener(new View.OnClickListener() {
            Intent bindIntent = new Intent(mContext, BluetoothActivity.class);
            @Override
            public void onClick(View view) {

                if(!PM_flag) {
                    locButton.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.colorPrimary)));
                    bindIntent.putExtra("PM", start_PM);
                    PM_flag=true;
                    startService(bindIntent);
                    //Bandera que permite la escritura automatica en Firebase
                    FB_flag = true;
                }

                else {
                    locButton.setBackgroundTintList(ColorStateList.
                            valueOf(getResources().getColor(R.color.colorWhite)));
                    bindIntent.putExtra("PM", finish_PM);
                    PM_flag=false;
                    startService(bindIntent);
                    FB_flag=false;

                }

            }
        });


        //Boton read Data from FB
        readButton = findViewById(R.id.btn_ReadFB);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               if(latLngList!=null && !realTime_flag) {
                        addHeatMap();
                    }

                if(realTime_flag){
                    updateLabel();
                }

            }
        });


        /** LOCALIZACIÓN Y GOOGLEMAP */


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

    /** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** */

    /** ACTIVITY LIFECYCLE */

    @Override
    protected void onResume(){

        super.onResume();
        FB_flag=false;

    }

    @Override
    protected void onPause(){

        super.onPause();

    }

    /** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** */

    /** METODOS LOCALIZACION Y GOOGLE MAP */


    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastlocation=location;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
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
            if(mMap_locationFlag) {
                mMap.setMyLocationEnabled(true);
            }
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

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    5,
                    locationListenerNetwork);

        }
    }



    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }




/*

    private void removeHeatMap(){

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .weightedData(latLngList)
                .build();
        TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        mOverlay.setVisible(true);

    }
*/


    private void addHeatMap(){

        if(latLngList.size()!=0) {
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .weightedData(latLngList)
                    .radius(25)
                    .build();
            mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            mOverlay.setVisible(true);
        }


    }




    /** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** */

    /** METODOS MODOS DE OPERACION */


    public void spinners(){
        adapter_maps =
                new ArrayAdapter<>(this, R.layout.spinner, tipos_mapa);
        adapter_maps.setDropDownViewResource(R.layout.spinner);
        mapsOptions.setAdapter(adapter_maps);

        adapter_mode =
                new ArrayAdapter<>(this, R.layout.spinner, modos_lst);
        adapter_mode.setDropDownViewResource(R.layout.spinner);
        mode.setAdapter(adapter_mode);

        adapter_time =
                new ArrayAdapter<>(this, R.layout.spinner, periodos);
        period.setAdapter(adapter_time);
        adapter_time.setDropDownViewResource(R.layout.spinner);


        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView_mode, View view, int i, long l) {
                long id = adapterView_mode.getItemIdAtPosition(i);

                //Caso Firebase
                if (id==0){
                    locButton.setVisibility(View.VISIBLE);
                    calButton.setVisibility(View.GONE);
                    fbButton.setVisibility(View.GONE);
                    readButton.setVisibility(View.GONE);
                    realTime_flag=false;
                }

                //Caso RealTime
                else if (id==1) {
                    locButton.setVisibility(View.GONE);
                    calButton.setVisibility(View.GONE);
                    fbButton.setVisibility(View.GONE);
                    readButton.setVisibility(View.VISIBLE);
                    realTime_flag=true;
                }

                //Caso Historico o fechas
                else if(id>1) {
                    calButton.setVisibility(View.VISIBLE);
                    locButton.setVisibility(View.GONE);
                    fbButton.setVisibility(View.VISIBLE);
                    readButton.setVisibility(View.VISIBLE);
                    realTime_flag=false;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //Funcion asignacion tipo de mapa
        mapsOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView_maps, View view, int i, long l) {
                long id = adapterView_maps.getItemIdAtPosition(i);

                //Obtenemos el indice de posicion del elemento seleccionado
                // y asignamos un mapa en funcion de dicho indice
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


    //Esta funcion se encarga de recoger la localizacion de OnLocationChanged
    // y temporizarsu escritura en la base de datos.
    //Esto lo hace esperando a que se determinen las condiciones del experimento
    // una vez hecho esto, genera automaticamente los registros

    private void save_location (Location location){
        lastlocation  = location;

        if (FB_flag) {
            if (lastlocation == null){
                Toast.makeText(getApplicationContext(), "Localización nula",
                        Toast.LENGTH_SHORT).show();
                changeProvider();

            }
            else { Intent bindIntent = new Intent(mContext, BluetoothActivity.class);
                bindIntent.putExtra("PM", read_PM);
                startService(bindIntent);
                if(PMData!=0 && save_loc_PM_flag) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Bundle b = new Bundle();
                    b.putDouble("latitud", latitude);
                    b.putDouble("longitud", longitude);
                    b.putInt("pm", PMData_array[PM_FB_counter]); //Si estoy en modo Firebase o Re
                    b.putString("date", today);
                    if (PM_FB_counter == PMData_max-1) {
                        PM_FB_counter = 0;
                    } else {
                        PM_FB_counter++;
                    }
                    save_loc_PM_flag=false; //Espero hasta la siguiente medida correcta
                    Intent i = new Intent(MapsActivity.this, FireBaseActivity.class);
                    i.putExtra("bundleFire", b);
                    startActivity(i);
                }
            }

        }

    }

    /** Modo HISTÓRICO */

    //Metodo para escribir la fecha en la pantalla

    private void updateLabel() {
        if(!realTime_flag) {
        adapter_mode.remove(dateSpinner);
        dateSpinner = sdf.format(myCalendar.getTime());
        adapter_mode.insert(dateSpinner,2);
        }
        dateSpinner = sdf.format(myCalendar.getTime());

        //Actualizo el valor de la referencia cada vez que se cambia de fecha

        dbLocations =FirebaseDatabase.getInstance().getReference().child("locations").child(dateSpinner);

        dbLocations.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                latLngList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    com.example.michel.bicimaps.Location loc;
                    loc = postSnapshot.getValue(com.example.michel.bicimaps.Location.class);
                    LatLng latLng = new LatLng(loc.getLat(), loc.getLon());
                    WeightedLatLng data = new WeightedLatLng(latLng, loc.getPm());
                    latLngList.add(data);
                }

                if(latLngList.size()== 0){
                    Toast.makeText(mContext, "No hay datos para la fecha elegida",
                            Toast.LENGTH_LONG).show(); } else {
                addHeatMap();
                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }






                /** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** */


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

            AlertDialog.Builder alertDialog=new AlertDialog.Builder(MapsActivity.this,
                    R.style.MyDialogTheme);
            alertDialog.setTitle("Enable Location");
            alertDialog.setMessage("Your location is not enabled." +
                    " Please enable it in settings menu.");
            alertDialog.setPositiveButton("Location Settings",
                    new DialogInterface.OnClickListener(){
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

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
                            R.style.MyDialogTheme);
                    builder.setMessage("Para el correcto funcionamiento de la aplicación," +
                            " debes aceptar el permiso de ubicación.")
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

   /** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** */


   /** MÉTODOS PARA CONEXIÓN CON SERVICIO BLUETOOTH*/


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
             PMData = intent.getIntExtra("TestData", PMDefault);
           if(PMData!=0) {
               save_loc_PM_flag=true; //He recibido una medida correcta!
               PMData_array[PMData_counter] = PMData;
               //Cuando llega a 10 elementos, sobreeescribo el vector
               if (PMData_counter == PMData_max-1) {
                   PMData_counter = 0;
               } else {
                   PMData_counter++;
               }
           }

        }


    };


    /** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** */


}