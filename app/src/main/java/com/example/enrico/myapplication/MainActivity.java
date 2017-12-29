package com.example.enrico.myapplication;


import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.view.ViewPager;

import com.firebase.client.Firebase;
import com.firebase.client.ServerValue;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionPagerAdapter; // for tab pages

    private DatabaseReference mUserRef;

    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("WeTalk"); // set title of action bar

        //Tabs
        mViewPager = (ViewPager) findViewById(R.id.tabPager);
        mSectionPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionPagerAdapter); // create each page

        mTabLayout = (TabLayout) findViewById(R.id.main_tab);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            sendToStart();
        } else {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    public void sendToStart(){
        Intent startIntent = new Intent( MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    @Override // creates main menu options
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // if signout is chose
        if(item.getItemId() == R.id.btn_menu_signout) {
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId() == R.id.btn_menu_settings){
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }

        if(item.getItemId() == R.id.btn_menu_users){
            Intent userIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(userIntent);
        }

        return true;
    }
}
