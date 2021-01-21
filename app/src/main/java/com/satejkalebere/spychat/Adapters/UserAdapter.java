package com.satejkalebere.spychat.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.satejkalebere.spychat.MessageActivity;
import com.satejkalebere.spychat.Model.Chat;
import com.satejkalebere.spychat.Model.Users;
import com.satejkalebere.spychat.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<Users> mUsers;
    private boolean isChat;

    String theLastMessage;
    String LastMsgTime;


    //Constructor


    public UserAdapter(Context context, List<Users> mUsers, boolean isChat) {
        this.context = context;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.user_item,
                parent,
                false);

        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {


        final Users users = mUsers.get(position);
        holder.username.setText(users.getUsername());


        if (users.getImageURL().equals("default")) {
            holder.imageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
        } else {
            //Adding Glide Library
            Glide.with(context)
                    .load(users.getImageURL())
                    .into(holder.imageView);
        }


        //Last Message
        if (isChat) {
            try {
                lastMessage(users.getId(), holder.last_msg, holder.last_msg_time);
            } catch (Exception e) {
                Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
            }

        } else {
            try {
                holder.last_msg.setVisibility(View.GONE);
            } catch (Exception e) {
                Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
            }

        }


        //Status Check
        if (isChat) {
            if (users.getStatus().equals("online")) {
                holder.imageViewON.setVisibility(View.VISIBLE);
                holder.imageViewOFF.setVisibility(View.GONE);
            } else {

                holder.imageViewOFF.setVisibility(View.VISIBLE);
                holder.imageViewON.setVisibility(View.GONE);

            }
        } else {
            holder.imageViewON.setVisibility(View.GONE);
            holder.imageViewOFF.setVisibility(View.GONE);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mUsers.clear();
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra("userid", users.getId());
                context.startActivity(i);


            }

        });


    }


    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    //Check For Last Message
    private void lastMessage(final String userid, final TextView last_msg, final TextView last_msg_time) {
        theLastMessage = "default";
        LastMsgTime = "d";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                            (chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid()))) {
                        theLastMessage = chat.getMessage();
                        LastMsgTime = chat.getMsg_time();
                    }

                    switch (theLastMessage) {
                        case "default":
                            last_msg.setText("");
                            last_msg_time.setVisibility(View.GONE);
                            break;

                        default:
                            try {
                                last_msg.setText(theLastMessage);
                                last_msg_time.setVisibility(View.VISIBLE);
                                last_msg_time.setText(LastMsgTime);
                                break;
                            } catch (Exception e) {
                                Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
                            }

                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public ImageView imageView;
        public ImageView imageViewON;
        public ImageView imageViewOFF;
        public TextView last_msg;
        public TextView last_msg_time;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.textView22);
            imageView = itemView.findViewById(R.id.imageViewUser);
            imageViewON = itemView.findViewById(R.id.status_imageView_on);
            imageViewOFF = itemView.findViewById(R.id.status_imageView_off);
            last_msg = itemView.findViewById(R.id.last_message);
            last_msg_time = itemView.findViewById(R.id.MsgTime);


        }
    }



}
