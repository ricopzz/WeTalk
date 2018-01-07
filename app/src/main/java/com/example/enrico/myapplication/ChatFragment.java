package com.example.enrico.myapplication;


import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mChatList;

    private DatabaseReference mChatDb;
    private DatabaseReference mMessageDb;
    private DatabaseReference mUserDb;

    private FirebaseAuth mAuth;
    private String mCurrent_Uid;
    private View mView;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_chat,container,false);

        mChatList = (RecyclerView) mView.findViewById(R.id.list_chatlist);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_Uid = mAuth.getCurrentUser().getUid();

        mChatDb = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_Uid);
        mChatDb.keepSynced(true);

        mUserDb = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDb = FirebaseDatabase.getInstance().getReference().child("Messages").child(mCurrent_Uid);
        mUserDb.keepSynced(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(linearLayoutManager);

        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();

        Query chatQuery = mChatDb.orderByChild("timestamp");

        FirebaseRecyclerOptions<Chat> options =
                new FirebaseRecyclerOptions.Builder<Chat>()
                        .setQuery(mMessageDb, Chat.class)
                        .build(); // set the query for adapter

        FirebaseRecyclerAdapter<Chat, ChatViewHolder> firebaseChatAdapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ChatViewHolder holder, int position, @NonNull final Chat model) {

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDb.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        String data = dataSnapshot.child("message").getValue().toString();
                        holder.setMessage(data, model.isSeen());
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

                mUserDb.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online")){
                            String online_status = dataSnapshot.child("online").getValue().toString();
                            holder.setUserOnline(online_status);
                        }

                        holder.setName(name);
                        holder.setUserImage(thumb_image,getContext());

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name",name);
                                startActivity(chatIntent);

                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.single_user_layout, parent, false);

                return new ChatViewHolder(view);
            }
        };
        firebaseChatAdapter.startListening();
        mChatList.setAdapter(firebaseChatAdapter);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            this.mView = itemView;
        }

        public void setMessage(String message, boolean isSeen){
            TextView userStatusView = (TextView) mView.findViewById(R.id.text_userlayout_status);
            userStatusView.setText(message);

            if(!isSeen){
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD);
            } else {
                userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL);
            }
        }

        public void setName(String name){
            TextView userNameView = (TextView) mView.findViewById(R.id.text_userlayout_displayname);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context ctx){
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_display_picture);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_user_icon).into(userImageView);
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
