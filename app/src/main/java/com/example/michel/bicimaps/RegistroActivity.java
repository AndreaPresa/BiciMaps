package com.example.michel.bicimaps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;


public class RegistroActivity extends AppCompatActivity {

    private TextView Login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        Login=(TextView)findViewById(R.id.backtologin);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent =
                        new Intent(RegistroActivity.this, MainActivity.class);
                startActivity(intent);
            }

        });


    }






}
