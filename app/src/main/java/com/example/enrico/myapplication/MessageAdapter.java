package com.example.enrico.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Created by enrico on 06/01/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;


    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType){

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent,false);

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder viewHolder, int position) {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser current_user = mAuth.getCurrentUser();

        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        Log.d("TAG",from_user);

        if(current_user != null){
            if(from_user.equals(current_user.getUid())){
                viewHolder.messageText.setBackgroundResource(R.drawable.message_text_sender_background);
                viewHolder.messageText.setTextColor(Color.BLACK);
            } else {
                viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
                viewHolder.messageText.setTextColor(Color.WHITE);
            }
            viewHolder.messageText.setText(c.getMessage());
            //viewHolder.timeText.setText(c.getTime());
        }


    }

    @Override
    public int getItemCount(){
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public TextView timeText;

        public MessageViewHolder(View view){
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_chat_from);
            timeText = (TextView) view.findViewById(R.id.message_chat_time);
        }
    }

}
