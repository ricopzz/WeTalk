package com.example.enrico.myapplication;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactFragment extends Fragment {

    private RecyclerView mFriendList;

    private DatabaseReference mFriendDb;
    private DatabaseReference mUserDb;
    private FirebaseAuth mAuth;
    private String mCurrentId;
    public View mMainView;

    public ContactFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView =  inflater.inflate(R.layout.fragment_contact, container, false);
        mAuth = FirebaseAuth.getInstance();
        mCurrentId = mAuth.getCurrentUser().getUid();
        mFriendList = (RecyclerView) mMainView.findViewById(R.id.list_friendlist);

        mFriendDb = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentId);
        mFriendDb.keepSynced(true);
        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDb.keepSynced(true);

        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Friends> options =
                new FirebaseRecyclerOptions.Builder<Friends>()
                        .setQuery(mFriendDb, Friends.class)
                        .build(); // set the query for adapter

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            public FriendsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder holder, int position, @NonNull final Friends model) {


                final String list_user_id = getRef(position).getKey();

                mUserDb.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String user_name = dataSnapshot.child("name").getValue().toString();
                        String user_status = dataSnapshot.child("status").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online_status = String.valueOf(dataSnapshot.child("online").getValue());
                            holder.setUserOnline(online_status);
                        }

                        holder.setName(user_name);
                        holder.setDisplayPicture(thumb_image, getContext());
                        holder.setStatus(user_status);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CharSequence options[] = new CharSequence[]{"View Profile","Send Message"};
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext()); // to pop options dialog
                                builder.setTitle("Select Options");
                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //click option
                                        if(which == 0){
                                            Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id", list_user_id);
                                            startActivity(profileIntent);
                                        }

                                        if(which==1){
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id", list_user_id);
                                            chatIntent.putExtra("name",user_name);
                                            startActivity(chatIntent);

                                        }

                                    }
                                });

                                builder.show();
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };
        firebaseRecyclerAdapter.startListening();
        mFriendList.setAdapter(firebaseRecyclerAdapter);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public FriendsViewHolder(View itemView){
            super(itemView);
            this.mView = itemView;
        }

        public void setStatus(String status){
            TextView statusView = (TextView) mView.findViewById(R.id.text_userlayout_status);
            statusView.setText(status);
        }

        public void setName(String name){
            TextView nameView = (TextView) mView.findViewById(R.id.text_userlayout_displayname);
            nameView.setText(name);
        }

        public void setDisplayPicture(String thumb_image, Context ctx){
            CircleImageView displaypicView = (CircleImageView) mView.findViewById(R.id.user_display_picture);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_user_icon).into(displaypicView);
        }

        public void setUserOnline(String icon_status){
            ImageView online_status = (ImageView) mView.findViewById(R.id.icon_userlayout_online);
            if(icon_status.equals("true")){
                online_status.setVisibility(View.VISIBLE);
            }
            else{
                online_status.setVisibility(View.INVISIBLE);
            }
        }
    }
}
