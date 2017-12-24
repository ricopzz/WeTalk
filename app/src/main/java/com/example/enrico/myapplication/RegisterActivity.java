package com.example.enrico.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends Activity {

    private static final String TAG = "MyActivity";

    private TextInputLayout mFullName;
    private TextInputLayout mEmail;
    private TextInputLayout mUsername;
    private TextInputLayout mPassword;
    private Button mReg;
    private TextView mBack;
    private TextView mSignIn;

    private ProgressDialog mRegProgress;

    //Firebae Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);

        //Firebase Auth
         mAuth = FirebaseAuth.getInstance();

         //Fields
        mFullName = (TextInputLayout) findViewById(R.id.reg_fullname);
        mEmail = (TextInputLayout) findViewById(R.id.reg_email);
        mUsername = (TextInputLayout) findViewById(R.id.reg_username);
        mPassword = (TextInputLayout) findViewById(R.id.reg_password);
        mReg = (Button) findViewById(R.id.btn_login);
        mBack = (TextView) findViewById(R.id.btn_reg_back);
        mSignIn = (TextView) findViewById(R.id.btn_reg_signin);
        mRegProgress = new ProgressDialog(this);

        mReg.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                String fullName = mFullName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                String username = mUsername.getEditText().getText().toString();

                if(!TextUtils.isEmpty(fullName) && !TextUtils.isEmpty(email ) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we are registering user");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(fullName, email, username, password);
                }
                else{
                    Toast.makeText(RegisterActivity.this,"Cannot Register. Please fill in all blanks.", Toast.LENGTH_LONG).show();
                }


            }
        });

        mBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                goToStart();
            }
        });

        mSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });
    }

    private void goToLogin(){
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void goToStart(){
        Intent backIntent = new Intent(RegisterActivity.this,StartActivity.class);
        startActivity(backIntent);
        finish();
    }

    private void register_user(String fullName, String email, String username, String password){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //Sign in success
                    mRegProgress.dismiss();

                    Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                } else {
                    mRegProgress.hide();
                    Toast.makeText(RegisterActivity.this,"Cannot Register. Invalid Email Address.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
