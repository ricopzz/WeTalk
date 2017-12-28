package com.example.enrico.myapplication;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mDisplayName, mStatus, mProfileFriendsCount;
    private Button mSendReq;

    private DatabaseReference mUserDb;
    private DatabaseReference mFriendReqDb;
    private DatabaseReference mFriendDb;
    private FirebaseUser mCurrentUser;

    private ProgressDialog mProgressDialog;

    private int current_state = 0; // 0 not friend, 1 req sent, 2 req received, 3 friends
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id = getIntent().getStringExtra("user_id");

        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDb = FirebaseDatabase.getInstance().getReference().child("Friend_Req");
        mFriendDb = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.image_profile_dp);
        mDisplayName = (TextView) findViewById(R.id.text_profile_displayname);
        mStatus = (TextView) findViewById(R.id.text_profile_status);
        mSendReq = (Button) findViewById(R.id.btn_profile_sendreq);
        mProfileFriendsCount = (TextView) findViewById(R.id.text_profile_totalfriend);



        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we are retrieving the data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.main_bg).into(mProfileImage);

                // FRIEND LIST / REQUEST FEATURE

                mFriendReqDb.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String request_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(request_type.equals("received")){
                                current_state = 2;
                                mSendReq.setText("Accept Request");
                            }
                            else if(request_type.equals("sent")){
                                current_state = 1;
                                mSendReq.setText("Cancel Request");
                            }

                            mProgressDialog.dismiss();

                        } else {
                            mFriendDb.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){
                                        current_state = 3;
                                        mSendReq.setText("Unfriend");
                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mSendReq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSendReq.setEnabled(false);
                // NOT FRIENDS
                if(current_state==0){
                    mFriendReqDb.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mFriendReqDb.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        current_state = 1;
                                                        mSendReq.setText("Cancel Request");
                                                        Toast.makeText(ProfileActivity.this,"Request Sent!",Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(ProfileActivity.this,"Fail to send request",Toast.LENGTH_LONG).show();
                                    }
                                    mSendReq.setEnabled(true);
                                }
                            });
                }

                // CANCEL REQUEST

                if(current_state==1){
                    mFriendReqDb.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDb.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendReq.setEnabled(true);
                                    current_state = 0;
                                    mSendReq.setText("Send Request");

                                }
                            });
                        }
                    });
                }

                // REQUEST RECEIVED

                if(current_state==2){
                    final String current_date = DateFormat.getDateInstance().format(new Date());
                    mFriendDb.child(mCurrentUser.getUid()).child(user_id).setValue(current_date)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDb.child(user_id).child(mCurrentUser.getUid()).setValue(current_date)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                            mFriendReqDb.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mFriendReqDb.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            mSendReq.setEnabled(true);
                                                            current_state = 3;
                                                            mSendReq.setText("Unfriend");

                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    });
                        }
                    });
                }

                // UNFRIEND

                if(current_state==3){
                    mFriendDb.child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDb.child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendReq.setEnabled(true);
                                    current_state = 0;
                                    mSendReq.setText("Send Request");
                                }
                            });
                        }
                    });
                }
            }
        });
    }

}
