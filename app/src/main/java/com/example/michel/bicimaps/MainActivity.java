package com.example.michel.bicimaps;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

        private Button btnEntrar;
        private TextView Registro;
        private Button btnEntrarFire;
        private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 1;


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
                Intent intent=
                        new Intent(MainActivity.this, RegistroActivity.class);
                startActivity(intent);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        isBluetoothEnabled();

    }

    public void isBluetoothEnabled() {

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "El dispositivo no es válido para esta app", Toast.LENGTH_LONG).show();
        }

        else if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);


        }


    }

    //Función que controla la respuesta a la activación del Bluetooth
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                mBluetoothAdapter.enable();

                Toast.makeText(MainActivity.this, "Bluetooth enabled correctly", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(MainActivity.this, "Error while enabling Bluetooth. Try again", Toast.LENGTH_SHORT).show();

            }
        }
    }
}
