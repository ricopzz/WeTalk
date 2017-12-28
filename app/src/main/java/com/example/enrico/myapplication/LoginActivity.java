package com.example.enrico.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private EditText mEmail;
    private EditText mPassword;
    private Button mBtnIn;
    private TextView mBtnSignup;
    private TextView mBtnBack;
    private TextView mFPass;
    private FirebaseAuth mAuth;
    private ProgressDialog mLoginProgress;
    private RSACryptography mRSA = new RSACryptography();
    private DatabaseReference mUserDb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        mEmail = (EditText) findViewById(R.id.txt_login_email);
        mPassword = (EditText) findViewById(R.id.txt_login_password);
        mBtnIn = (Button) findViewById(R.id.btn_login);
        mBtnBack = (TextView) findViewById(R.id.btn_login_back);
        mBtnSignup = (TextView) findViewById(R.id.btn_login_singup);
        mFPass = (TextView) findViewById(R.id.btn_login_fpass);
        mLoginProgress = new ProgressDialog(this);
        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users");

        mBtnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSignUp();
            }
        });

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStart();
            }
        });

        mBtnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();

                byte[] convertEmail = mRSA.encrypt(email.getBytes());

                String encrypted = convertEmail.toString();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials");
                    mLoginProgress.show();
                    signInUser(email,password);
                }
                else{
                    Toast.makeText(LoginActivity.this,"Cannot sign in. Please fill in email and password",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void signInUser(String email, String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    mLoginProgress.dismiss();

                    String current_uid = mAuth.getCurrentUser().getUid();

                    // SAVE THE PHONE'S TOKEN ID
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    mUserDb.child(current_uid).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                            }
                        }
                    });
                    goToMain();
                } else {
                    mLoginProgress.hide();
                    Toast.makeText(LoginActivity.this,"Cannot sign in. Please check your email or password", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void goToMain() {
        Intent goIntent = new Intent(LoginActivity.this,MainActivity.class);
        goIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goIntent);
        finish();
    }

    private void goToStart(){
        Intent backIntent = new Intent(LoginActivity.this,StartActivity.class);
        startActivity(backIntent);
        finish();
    }

    private void goToSignUp(){
        Intent signupIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(signupIntent);
        finish();
    }

}
