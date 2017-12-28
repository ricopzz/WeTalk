package com.example.enrico.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.view.View;
import android.content.Intent;

public class StartActivity extends AppCompatActivity {

    private Button mSignBtn;
    private Button mRegBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);


        mSignBtn = (Button)findViewById(R.id.btn_start_signin);
        mRegBtn = (Button) findViewById(R.id.btn_start_reg);

        mSignBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                Intent sign_intent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(sign_intent);
            }
        });

        mRegBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){

                Intent reg_intent = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(reg_intent);
            }
        });
    }
}
