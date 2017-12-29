package com.example.enrico.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mReceiverUserID;
    private String mReceiverName;

    private Toolbar mToolbar;

    private DatabaseReference mRootRef;
    private FirebaseAuth mAuth;
    private String mCurrentUserID;

    private TextView mName;
    private TextView mLastSeen;
    private CircleImageView mDisplayPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mReceiverUserID = getIntent().getStringExtra("user_id");
        mReceiverName = getIntent().getStringExtra("name");
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserID = mAuth.getCurrentUser().getUid();


        mToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        //create custom bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_bar,null);
        //assign the custom bar
        actionBar.setCustomView(action_bar_view);

        // CUSTOM ACTION BAR ITEMS
        mName = (TextView) findViewById(R.id.chat_receivername);
        mLastSeen = (TextView) findViewById(R.id.chat_lastseen);
        mDisplayPic = (CircleImageView) findViewById(R.id.chat_image);

        mName.setText(mReceiverName);

        mRootRef.child("Users").child(mReceiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                if(online.equals("true")){
                    mLastSeen.setText("Online");
                    Picasso.with(ChatActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_user_icon).into(mDisplayPic, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(ChatActivity.this).load(image).placeholder(R.drawable.default_user_icon).into(mDisplayPic);
                        }
                    });
                } else {
                    GetTimeAgo getTimeAgo = new GetTimeAgo();

                    long lastTime = Long.parseLong(online);
                    String last_seen = getTimeAgo.getTimeAgo(lastTime, getApplicationContext());
                    mLastSeen.setText("Last online: "+last_seen);
                    Picasso.with(ChatActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.main_bg).into(mDisplayPic);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(mReceiverUserID)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatAddMap.put("Chat/" + mCurrentUserID + "/" + mReceiverUserID, chatAddMap);
                    chatAddMap.put("Chat/" + mReceiverUserID + "/" + mCurrentUserID, chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Log.d("CHAT_LOG", databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
