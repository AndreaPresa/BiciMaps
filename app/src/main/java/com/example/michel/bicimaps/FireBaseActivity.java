package com.example.michel.bicimaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;



/* 9/05 No consigo crear hijos en Firebase: por lo que parece hay un problema con el contador_locs*/
      /*      Tampoco consigo que se escriban automaticamente una vez iniciada la actividad de Firebase*/
      /*      Se puede controlar el tiempo de los LocationRequest tocando los intervalos
      * No se si es por el RecyclerView porque parece que en Firebase si los crea pero luego en el movil
      * no los muestra*/





    //10/05 Arreglado. Era un problema de la escritura en Firebase, que estaba sobreescribiendo la misma localizacion
    //Los 1000ms del onLocationChanged (LocationRequest) son 5 segundos reales.
    //IMPORTANTE: LAUNCHMODE de FirebaseActivity es SingleTop.

    //Parece que ahora los escribe de dos en dos los elementos. Hay que conseguir controlar la localizacion
    //creando una clase LocationManager o algo así para que escribamos cada x tiempo y podamos parar y reanudar los
    //experimentos.


public class FireBaseActivity extends AppCompatActivity {

/*
    private static final String TAGLOG = "firebase-db";
*/

    private int contador_locs = 0;
    private RecyclerView recycler;
    private DatabaseReference dbLocations;

    private FloatingActionButton clear_button;
    private Calendar calendar; //Para recoger fecha y hora
    private SimpleDateFormat df;
    private String formattedDate;

    private String dateMaps;
    private TextView dateTitle;

    private FirebaseRecyclerAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_base);

        //Creo el formato para apuntar la fecha y la hora del experimento en FB
        df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        clear_button = (FloatingActionButton) findViewById(R.id.btn_backToMap);

        //Recibo el primer intent al crear la actividad
        Intent intent = getIntent();
        intent.getExtras();

        dateTitle = (TextView) findViewById(R.id.dateTitle);

        dbLocations =
                FirebaseDatabase.getInstance().getReference()
                        .child("locations");


          clear_button.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          dbLocations.removeValue();
                          Intent i = new Intent(FireBaseActivity.this, MapsActivity.class);
/*
                          //Los modos de Flag son necesarios para que FB_flag no cambie de valor solo...
                          //Lo que se hace es que se crea una nueva task y se eliminan las demás actividades
*/                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                          startActivity(i);
                          onPause();

                      }
                  });


        RecyclerView recycler = findViewById(R.id.lstLocations);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);


        //Creo el bundle que recibira la nueva localizacion para apuntarla
        if (intent.getExtras()!=null) {

            Bundle bundle = intent.getBundleExtra("bundleFire");
            double lat = bundle.getDouble("latitud");
            double lon = bundle.getDouble("longitud");
            int pm = bundle.getInt("pm");
            dateMaps = bundle.getString("date");
            dateTitle.setText(dateMaps);
            calendar = Calendar.getInstance();
            if(calendar!=null){
            formattedDate = df.format(calendar.getTime());}
            else {formattedDate = " ";}

            double lat_r = (double) Math.round(lat * 100000) / 100000;
            double lon_r = (double) Math.round(lon * 100000) / 100000;


            Location newLocation = new Location();
            newLocation.setLat(lat_r);
            newLocation.setLon(lon_r);
            newLocation.setPm(pm);
            newLocation.setDh(formattedDate);


            //Metemos un contador para ir añadiendo localizaciones

            String loc_1 = "loc";
            String loc_2 =  contador_locs + loc_1;
            writeNewLocation(loc_2, newLocation);
            contador_locs++;

        }

        if(dateMaps!=null) {


            mAdapter =
                    new FirebaseRecyclerAdapter<Location, LocationHolder>(
                            Location.class, R.layout.layout_fb_adapter, LocationHolder.class, dbLocations.child(dateMaps)) {


                        @Override
                        public void populateViewHolder(LocationHolder locViewholder, Location loc, int position) {
                            locViewholder.setLatitud(loc.getLat());
                            locViewholder.setLongitud(loc.getLon());
                            locViewholder.setPM(loc.getPm());
                            locViewholder.setDH(loc.getDh());

                        }
                    };

            recycler.setAdapter(mAdapter);
        }


    }


    //Este metodo recoge el intent que se le envia de nuevo al ser LaunchMode=SingleTop

    protected void onNewIntent(Intent intent){


         intent.getExtras();

              if (intent.getExtras()!=null) {


                  Bundle bundle = intent.getBundleExtra("bundleFire");
                  double lat = bundle.getDouble("latitud");
                  double lon = bundle.getDouble("longitud");
                  int pm = bundle.getInt("pm");
                  calendar = Calendar.getInstance();
                  if(calendar!=null){
                      formattedDate = df.format(calendar.getTime());}
                  else {formattedDate = " ";}

                  double lat_r = (double) Math.round(lat * 100000) / 100000;
                  double lon_r = (double) Math.round(lon * 100000) / 100000;



                  Location newLocation = new Location();
                  newLocation.setLat(lat_r);
                  newLocation.setLon(lon_r);
                  newLocation.setPm(pm);
                  newLocation.setDh(formattedDate);

                  //Metemos un contador para ir añadiendo localizaciones


                  String loc_1 = "loc";
                  String loc_2 =  contador_locs + loc_1;
                  writeNewLocation(loc_2, newLocation);
                  contador_locs++;



              }
    }

    @Override
    protected void onResume(){

        super.onResume();


    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

       /* mAdapter.cleanup();*/
    }


    private void writeNewLocation(String locs, Location loc) {

        //el metodo push() permite crear nuevos hijos sin sobreescribirlos
          String dateFB = loc.getDh();
          dateFB= dateFB.substring(0,10);
          dbLocations.push().child(dateFB);
          dbLocations.child(dateFB).push().child(locs);
          dbLocations.child(dateFB).child(locs).setValue(loc);



    }


}