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
import java.util.HashMap;
import java.util.Map;

import com.firebase.client.Firebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class RegisterActivity extends Activity {

    private static final String TAG = "MyActivity";

    private TextInputLayout mFullName;
    private TextInputLayout mEmail;
    private TextInputLayout mUsername;
    private TextInputLayout mPassword;
    private Button mReg;
    private TextView mBack;
    private TextView mSignIn;
    private RSACryptography mRsa = new RSACryptography();

    private ProgressDialog mRegProgress;

    private DatabaseReference mDatabase;
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

                byte[] encrypted = mRsa.encrypt(password.getBytes());

                if(!TextUtils.isEmpty(fullName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we are registering user");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    register_user(fullName,email, username, password);
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

    private void register_user(final String fullName, String email, final String username, final String password){

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();

                    HashMap<String, String> usermap = new HashMap<>();
                    byte[] encryptedName = mRsa.encrypt(fullName.getBytes());
                    byte[] encryptedStatus = mRsa.encrypt("Hi there, I'm using We Talk!".getBytes());
                    byte[] encryptedUsername = mRsa.encrypt(username.getBytes());
                    byte[] encryptedImage = mRsa.encrypt("https://ind.proz.com/zf/images/default_user_512px.png".getBytes());
                    byte[] encryptedThumb = mRsa.encrypt("default".getBytes());
                    usermap.put("name",fullName);
                    usermap.put("status","Hi there, I'm using We Talk!");
                    usermap.put("image","https://ind.proz.com/zf/images/default_user_512px.png");
                    usermap.put("username",username);
                    usermap.put("thumb_image","DEFAULT");
                    usermap.put("device_token",deviceToken);

                    mDatabase.setValue(usermap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                //Sign in success
                                mRegProgress.dismiss();

                                Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(mainIntent);
                                finish();
                            }
                            else{
                                mRegProgress.dismiss();
                            }
                        }
                    });

                } else {
                    mRegProgress.hide();
//                    try {
//                        throw task.getException();
//                    } catch(FirebaseAuthWeakPasswordException e) {
//                        mPassword.setError(getString(R.string.error_incorrect_password));
//                        mPassword.requestFocus();
//                    } catch(FirebaseAuthInvalidCredentialsException e) {
//                        mEmail.setError(getString(R.string.error_invalid_email));
//                        mEmail.requestFocus();
//                    } catch(FirebaseAuthUserCollisionException e) {
//                        mEmail.setError(getString(R.string.error));
//                        mEmail.requestFocus();
//                    } catch(Exception e) {
//                        Log.e(TAG, e.getMessage());
//                    }
                    Toast.makeText(RegisterActivity.this,"Cannot Register. Invalid Email Address.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
