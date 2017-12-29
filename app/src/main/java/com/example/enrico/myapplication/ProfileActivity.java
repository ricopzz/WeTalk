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
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mDisplayName, mStatus, mProfileFriendsCount;
    private Button mSendReq, mDecline;

    private DatabaseReference mUserDb;
    private DatabaseReference mFriendReqDb;
    private DatabaseReference mFriendDb;
    private DatabaseReference mNotifDb;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;

    private FirebaseAuth mAuth;

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
        mNotifDb = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mProfileImage = (ImageView) findViewById(R.id.image_profile_dp);
        mDisplayName = (TextView) findViewById(R.id.text_profile_displayname);
        mStatus = (TextView) findViewById(R.id.text_profile_status);
        mSendReq = (Button) findViewById(R.id.btn_profile_sendreq);
        mProfileFriendsCount = (TextView) findViewById(R.id.text_profile_totalfriend);
        mDecline = (Button) findViewById(R.id.btn_profile_declinereq);

        mDecline.setVisibility(View.INVISIBLE);
        mDecline.setEnabled(false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading user data");
        mProgressDialog.setMessage("Please wait while we are retrieving the data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map declineMap = new HashMap();
                declineMap.put("Friend_Req/" + mCurrentUser.getUid() + "/" + user_id, null);
                declineMap.put("Friend_Req/" + user_id + "/" + mCurrentUser.getUid(), null);

                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if(databaseError == null){
                            mSendReq.setEnabled(true);
                            current_state = 0;
                            mSendReq.setText("Send Request");

                            mDecline.setVisibility(View.INVISIBLE);
                            mDecline.setEnabled(false);
                        } else {
                            Toast.makeText(ProfileActivity.this, "Failed to decline due to some errors", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        });

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
                                mDecline.setVisibility(View.VISIBLE);
                                mDecline.setEnabled(true);
                            }
                            else if(request_type.equals("sent")){
                                current_state = 1;
                                mSendReq.setText("Cancel Request");
                                mDecline.setVisibility(View.INVISIBLE);
                                mDecline.setEnabled(false);
                            }

                            mProgressDialog.dismiss();

                        } else {
                            mFriendDb.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if(dataSnapshot.hasChild(user_id)){
                                        current_state = 3;
                                        mSendReq.setText("Unfriend");
                                        mDecline.setVisibility(View.INVISIBLE);
                                        mDecline.setEnabled(false);
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

                    DatabaseReference newNotificationRef = mRootRef.child("Notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notif_data = new HashMap<>();
                    notif_data.put("from", mCurrentUser.getUid());
                    notif_data.put("type", "request");

                    Map requestMap = new HashMap<>();
                    requestMap.put("Friend_Req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type","sent");
                    requestMap.put("Friend_Req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type","received");
                    requestMap.put("Notifications/" + user_id + "/" + newNotificationId, notif_data);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null){
                                current_state = 1;
                                mSendReq.setEnabled(true);
                                mSendReq.setText("Cancel Request");

                                mDecline.setVisibility(View.INVISIBLE);
                                mDecline.setEnabled(false);
                                Toast.makeText(ProfileActivity.this,"Request Sent!",Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(ProfileActivity.this, "There are some errors in seding request", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });

                }

                /* BEFORE
                    mFriendReqDb.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        mFriendReqDb.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received")
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        HashMap<String, String> notif_data = new HashMap<>();
                                                        notif_data.put("from", mCurrentUser.getUid());
                                                        notif_data.put("type", "request");

                                                        mNotifDb.child(user_id).push().setValue(notif_data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){

                                                                }
                                                            }
                                                        });

                                                        current_state = 1;
                                                        mSendReq.setText("Cancel Request");

                                                        mDecline.setVisibility(View.INVISIBLE);
                                                        mDecline.setEnabled(false);
                                                        Toast.makeText(ProfileActivity.this,"Request Sent!",Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(ProfileActivity.this,"Fail to send request",Toast.LENGTH_LONG).show();
                                    }
                                    mSendReq.setEnabled(true);
                                }
                            });
                 */

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

                                    mDecline.setVisibility(View.INVISIBLE);
                                    mDecline.setEnabled(false);

                                }
                            });
                        }
                    });
                }

                // REQUEST RECEIVED

                if(current_state==2){
                    final String current_date = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", current_date);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", current_date);

                    friendsMap.put("Friend_Req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_Req/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mSendReq.setEnabled(true);
                                current_state = 3;
                                mSendReq.setText("Unfriend");
                                mDecline.setVisibility(View.INVISIBLE);
                                mDecline.setEnabled(false);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to cancel request", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                    /* BEFORE
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
                                                            mDecline.setVisibility(View.INVISIBLE);
                                                            mDecline.setEnabled(false);
                                                        }
                                                    });
                                                }
                                            });

                                        }
                                    });
                        }

                    }); */
                }

                // UNFRIEND

                if(current_state==3){

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null){
                                mSendReq.setEnabled(true);
                                current_state = 0;
                                mSendReq.setText("Send Request");

                                mDecline.setVisibility(View.INVISIBLE);
                                mDecline.setEnabled(false);
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to unfriend due to some errors", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                    /* BEFORE
                    mFriendDb.child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDb.child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mSendReq.setEnabled(true);
                                    current_state = 0;
                                    mSendReq.setText("Send Request");

                                    mDecline.setVisibility(View.INVISIBLE);
                                    mDecline.setEnabled(false);
                                }
                            });
                        }
                    }); */
                }
            }
        });

    }
}
