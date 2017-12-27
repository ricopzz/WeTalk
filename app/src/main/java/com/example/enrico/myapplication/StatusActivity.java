package com.example.enrico.myapplication;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;
    private ProgressDialog mProgress;

    private DatabaseReference mStatusDb;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.status_app_bar);
        mSaveBtn = (Button) findViewById(R.id.btn_status_save);
        mStatus = (TextInputLayout) findViewById(R.id.txt_status_input);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = mCurrentUser.getUid();
        mStatusDb = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        String status_value = getIntent().getStringExtra("status_value");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status Message");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus.getEditText().setText(status_value);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress = new ProgressDialog(StatusActivity.this);

                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("Please wait while we save the changes");
                mProgress.show();

                String status = mStatus.getEditText().getText().toString();

                mStatusDb.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                        } else {
                            Toast.makeText(getApplicationContext(),"There was error in the changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

}
