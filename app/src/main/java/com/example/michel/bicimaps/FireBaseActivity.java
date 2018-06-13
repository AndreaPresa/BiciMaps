package com.example.michel.bicimaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;



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

    FirebaseRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fire_base);

        clear_button = (FloatingActionButton) findViewById(R.id.btn_backToMap);



        Intent intent = getIntent();
        intent.getExtras();

        dbLocations =
                FirebaseDatabase.getInstance().getReference()
                        .child("locations");


          clear_button.setOnClickListener(new View.OnClickListener() {
                      @Override
                      public void onClick(View view) {
                          dbLocations.removeValue();
                          Intent i = new Intent(FireBaseActivity.this, MapsActivity.class);
                          i.putExtra("FBflag",false);
                          startActivity(i);
                          onDestroy();

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



            Location newLocation = new Location();
            newLocation.setLat(lat);
            newLocation.setLon(lon);

            //Metemos un contador para ir añadiendo localizaciones


            String loc_1 = "loc";
            String loc_2 =  contador_locs + loc_1;
            writeNewLoc(loc_2, newLocation);

            contador_locs++;

        }

        mAdapter =
                new FirebaseRecyclerAdapter<Location, LocationHolder>(
                        Location.class, R.layout.listitem_titular, LocationHolder.class, dbLocations) {


                    @Override
                    public void populateViewHolder(LocationHolder locViewholder, Location loc, int position) {
                        /*locViewholder.setPosition(position);*/
                        locViewholder.setLatitud(loc.getLat());
                        locViewholder.setLongitud(loc.getLon());

                    }
                };

        recycler.setAdapter(mAdapter);


    }


    //Este metodo recoge el intent que se le envia de nuevo al ser LaunchMode=SingleTop
    protected void onNewIntent(Intent intent){


         intent.getExtras();

              if (intent.getExtras()!=null) {


                  Bundle bundle = intent.getBundleExtra("bundleFire");
                  double lat = bundle.getDouble("latitud");
                  double lon = bundle.getDouble("longitud");



                  Location newLocation = new Location();
                  newLocation.setLat(lat);
                  newLocation.setLon(lon);

                  //Metemos un contador para ir añadiendo localizaciones


                  String loc_1 = "loc";
                  String loc_2 =  contador_locs + loc_1;
                  writeNewLoc(loc_2, newLocation);
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


    private void writeNewLoc(String locs, Location loc) {

        //el metodo push() permite crear nuevos hijos sin sobreescribirlos

          dbLocations.push().child(locs);
          dbLocations.child(locs).setValue(loc);

    }


}