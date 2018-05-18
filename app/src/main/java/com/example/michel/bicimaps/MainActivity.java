package com.example.michel.bicimaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

        private Button btnEntrar;
        private TextView Registro;
        private Button btnEntrarFire;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Referencias a los controles que se encuentran en la actividad principal
        btnEntrarFire=findViewById(R.id.BtnEntrarFirebase);
        btnEntrar=findViewById(R.id.BtnEntrar);
        Registro=findViewById(R.id.Reg2Txt);


        //Listeners de los botones que inician actividades FireBase, Maps y Registro
        btnEntrarFire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =
                        new Intent(MainActivity.this, FireBaseActivity.class);
                startActivity(intent);
            }
        });


        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =
                        new Intent(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });

        Registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1=
                        new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent1);
            }
        });

    }
}
