package com.example.enrico.myapplication;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private ImageButton mUploadImage;
    private ImageButton mSend;
    private EditText mTextSent;

    private RecyclerView mMessagesView;
    private SwipeRefreshLayout mRefreshLayout;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter messageAdapter;

    private static final int TOTAL_ITEMS_LOADED = 20;
    private int mCurrentPage = 1;

    //save last loaded message
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";

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

        mUploadImage = (ImageButton) findViewById(R.id.chat_addimage);
        mSend = (ImageButton) findViewById(R.id.chat_send);
        mTextSent = (EditText) findViewById(R.id.chat_message_input);

        messageAdapter = new MessageAdapter(messagesList);

        mMessagesView = (RecyclerView) findViewById(R.id.messages_view);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.message_refresh_layout);
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesView.setHasFixedSize(true);
        mMessagesView.setLayoutManager(mLinearLayout);

        mMessagesView.setAdapter(messageAdapter);

        loadMessages();

        mName.setText(mReceiverName);

        // IDK WHY THIS DOESNT WORK
        mRootRef.child("Chat").child(mCurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(!dataSnapshot.hasChild(mReceiverUserID)){
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserID + "/" + mReceiverUserID, chatAddMap);
                    chatUserMap.put("Chat/" + mReceiverUserID + "/" + mCurrentUserID, chatAddMap);

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

        // TO SHOW ONLINE STATUS
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


        // BUTTON CLICK

        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;
                itemPos = 0;
                loadNextMessages();
            }
        });
    }


    private void sendMessage(){
        String message = mTextSent.getText().toString();


        if(!TextUtils.isEmpty(message)){

            String current_user_ref = "Messages/" + mCurrentUserID + "/" + mReceiverUserID;
            String receiver_user_ref = "Messages/" + mReceiverUserID + "/" + mCurrentUserID;

            DatabaseReference user_message_push = mRootRef.child("Messages").child(mCurrentUserID).child(mReceiverUserID).push();

            String push_id = user_message_push.getKey();

            Map messageSenderMap = new HashMap();
            messageSenderMap.put("message", message);
            messageSenderMap.put("seen", false);
            messageSenderMap.put("type", "text");
            messageSenderMap.put("time", ServerValue.TIMESTAMP);
            messageSenderMap.put("from", mCurrentUserID);

            Map messageReceiverMap = new HashMap();
            messageReceiverMap.put(current_user_ref + "/" + push_id, messageSenderMap);
            messageReceiverMap.put(receiver_user_ref + "/" + push_id, messageSenderMap);

            mTextSent.setText("");

            mRootRef.updateChildren(messageReceiverMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if(databaseError!=null){
                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                    }
                }
            });

        }
    }

    private void loadNextMessages(){
        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserID).child(mReceiverUserID);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(20);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if(!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++, message);
                } else {
                    mPrevKey = mLastKey;
                }

                if(itemPos == 1){
                    mLastKey = messageKey;
                }


                messageAdapter.notifyDataSetChanged();
                mMessagesView.scrollToPosition(messagesList.size() - 1); // shows the botto of recycler view
                mRefreshLayout.setRefreshing(false);
                mLinearLayout.scrollToPositionWithOffset(itemPos,0);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages(){

        DatabaseReference messageRef = mRootRef.child("Messages").child(mCurrentUserID).child(mReceiverUserID);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_LOADED); // limit the query to only shows the last number of conv

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages message = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                itemPos++;

                if(itemPos == 1){
                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }

                messagesList.add(message);
                messageAdapter.notifyDataSetChanged();

                mMessagesView.scrollToPosition(messagesList.size() - 1); // shows the botto of recycler view

                mRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
