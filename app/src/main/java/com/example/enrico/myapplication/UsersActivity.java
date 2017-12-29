package com.example.enrico.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUserList;

    private DatabaseReference mUserDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.users_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUserList = (RecyclerView) findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users");


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Users> options =
                new FirebaseRecyclerOptions.Builder<Users>()
                        .setQuery(mUserDb, Users.class)
                        .build(); // set the query for adapter

        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(options){
        //create the adapter
            @Override
            public UsersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new UsersViewHolder(view); // pass the view for the recyclerview
            }

            @Override // used to set all data in recyclerview
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull Users model) {
                holder.setName(model.getName());
                holder.setStatus(model.getStatus());
                holder.setDisplayPicture(model.getThumb_image(), getApplicationContext());

                final String user_id = getRef(position).getKey();

                holder.mView.setOnClickListener(new View.OnClickListener() { // to make the user profile clickable & go to its profile
                    @Override
                    public void onClick(View v) {

                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id", user_id);
                        startActivity(profileIntent);

                    }
                });
            }
        };
        firebaseRecyclerAdapter.startListening();
        mUserList.setAdapter(firebaseRecyclerAdapter);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    //class for the holder
    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView picView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name){
            TextView displayNameView = (TextView) mView.findViewById(R.id.text_userlayout_displayname);
            displayNameView.setText(name);
        }

        public void setStatus(String status){
            TextView statusView = (TextView) mView.findViewById(R.id.text_userlayout_status);
            statusView.setText(status);
        }

        public void setDisplayPicture(String thumb_image, Context ctx){
            CircleImageView displaypicView = (CircleImageView) mView.findViewById(R.id.user_display_picture);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_user_icon).into(displaypicView);
        }
    }

}
