package com.example.michel.bicimaps;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {

        private static final String TAG ="SIGN IN FAILED" ;
        private Button btnEntrar;
        private TextView Registro;
        private Button btnEntrarFire;
        private SignInButton btnEntrarGoogle;
        private BluetoothAdapter mBluetoothAdapter;
        private static final int REQUEST_ENABLE_BT = 1;
        private static final int RC_SIGN_IN = 2;
        private Context mContext;



        private GoogleSignInClient mGoogleSignInClient;
        private DatabaseReference dbUsers;

        private String user="User";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = this;

        //Referencias a los controles que se encuentran en la actividad principal
        btnEntrarFire=findViewById(R.id.BtnEntrarFirebase);
        btnEntrar=findViewById(R.id.BtnEntrar);
        btnEntrarGoogle=findViewById(R.id.BtnEntrarGoogle);
        Registro=findViewById(R.id.Reg2Txt);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
/*
                .requestIdToken(getString(R.string.google_credentials))
*/
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(mContext, gso);



        //Listeners de los botones que inician actividades FireBase, Maps, Maps con Google y Registro
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

        btnEntrarGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Configure sign-in to request the user's ID, email address, and basic
                // profile. ID and basic profile are included in DEFAULT_SIGN_IN.



                // Check for existing Google Sign In account, if the user is already signed in
                // the GoogleSignInAccount will be non-null.
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(mContext);

                if (account != null) {
                    Intent intent =
                            new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(intent);
                }
                else {
                    signIn();
                }
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
        switch (requestCode) {
            case REQUEST_ENABLE_BT:

                // Make sure the request was successful
                if (resultCode == RESULT_OK) {
                    mBluetoothAdapter.enable();

                    Toast.makeText(MainActivity.this, "Bluetooth enabled correctly", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Error while enabling Bluetooth. Try again", Toast.LENGTH_SHORT).show();

                }

                break;

            case RC_SIGN_IN:
                // The Task returned from this call is always completed, no need to attach
                // a listener.
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    saveAccount(account);
                    Intent intent =
                            new Intent(MainActivity.this, MapsActivity.class);
                    startActivity(intent);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w(TAG, "Google sign in failed", e);
                    // ...
                    signinFailed();

                }

                break;

            default:

                break;

        }
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    private void signinFailed(){
        Toast.makeText(mContext,"Sign in Failed. Try again",Toast.LENGTH_LONG ).show();
    }

    private void saveAccount(GoogleSignInAccount account){
        dbUsers =
                FirebaseDatabase.getInstance().getReference()
                        .child("users");


        String name = account.getDisplayName();
        String familyName = account.getFamilyName();
        String email = account.getEmail();
        String id = account.getId();

        user=id+user;

        dbUsers.push().child(user);
        dbUsers.child(user).setValue(name);

    }

    @Override
    public void onStop(){
        super.onStop();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }




}
