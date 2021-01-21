package com.satejkalebere.spychat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.satejkalebere.spychat.Adapters.MessageAdapter;
import com.satejkalebere.spychat.Fragments.APIService;
import com.satejkalebere.spychat.Model.Chat;
import com.satejkalebere.spychat.Model.Users;
import com.satejkalebere.spychat.Notifications.Client;
import com.satejkalebere.spychat.Notifications.Data;
import com.satejkalebere.spychat.Notifications.MyResponse;
import com.satejkalebere.spychat.Notifications.Sender;
import com.satejkalebere.spychat.Notifications.Token;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    TextView username;
    ImageView imageView, Back;
    TextView lastseen;


//    Toolbar toolbar;


    RecyclerView recyclerView;
    EditText msg_edittext;
    ImageButton sendBtn;


    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;

    MessageAdapter messageAdapter;
    List<Chat> mChat;

    String userid;


    APIService apiService;


    boolean notify = false;


    private byte encryptionkey[] = {8, 111, 54, 86, 105, 4, -37, -25, -65, 87, 18, 26, 5, -105, 114, -52};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;


    ValueEventListener seenListener;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);


        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        //widgets
        imageView = findViewById(R.id.imageview_profile);
        username = findViewById(R.id.username_profile);
        lastseen = findViewById(R.id.lastseen);
        Back = findViewById(R.id.backpress);


        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MessageActivity.this, ViewUserProfile.class);

                i.putExtra("userid", userid);

                startActivity(i);
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MessageActivity.this, ViewUserProfile.class);

                i.putExtra("userid", userid);
                startActivity(i);
            }
        });
        lastseen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MessageActivity.this, ViewUserProfile.class);
                i.putExtra("userid", userid);
                startActivity(i);
            }
        });


        sendBtn = findViewById(R.id.btn_send);
        msg_edittext = findViewById(R.id.text_send);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);





//        //Toolbar
//        Toolbar toolbar=findViewById(R.id.toolbar_profile);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });


        intent = getIntent();
         userid = intent.getStringExtra("userid");


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);

        try {
            cipher=Cipher.getInstance("AES");
            decipher=Cipher.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        secretKeySpec=new SecretKeySpec(encryptionkey,"AES");



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);

                username.setText(user.getUsername());

                if (user.getLastseen().equals("none")) {
                    lastseen.setVisibility(View.GONE);
                } else if (user.getLastseen().equals("Last seen: No Last Seen")) {
                    lastseen.setVisibility(View.GONE);
                } else {
                    lastseen.setText(user.getLastseen());
                    lastseen.setVisibility(View.VISIBLE);
                }


                if (user.getImageURL().equals("default")) {
                    imageView.setImageResource(R.drawable.ic_baseline_account_circle_24_white);
                } else {
                    try {
                        Glide.with(MessageActivity.this)
                                .load(user.getImageURL())
                                .into(imageView);
                    } catch (Exception e) {

                        Toast.makeText(MessageActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                    }


                }

                try {
                    readMessages(fuser.getUid(),userid,user.getImageURL());
                }catch (Exception e){

                    Toast.makeText(MessageActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                notify = true;
                String msg_time;
                String msg = encryptionMethod(msg_edittext.getText().toString());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Date date = Calendar.getInstance().getTime();
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a\ndd-MM-yyyy");
                    String strDate = dateFormat.format(date);
                    msg_time = strDate;
                } else {
                    msg_time = "";
                }
                String checkForEmpty = msg_edittext.getText().toString();
                if (!checkForEmpty.equals("")) {
                    try {
                        sendMessage(fuser.getUid(), userid, msg, msg_time);
                    } catch (Exception e) {

                        Toast.makeText(MessageActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    Toast.makeText(MessageActivity.this, "Type a message!!", Toast.LENGTH_SHORT).show();
                }
                msg_edittext.setText("");
            }
        });


        try {
            SeenMessage(userid);
        }catch (Exception e){
            Toast.makeText(MessageActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
        }

    }

    private void SeenMessage(final String userid){

        reference=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                    Chat chat=dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid)){


                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);

                        dataSnapshot.getRef().updateChildren(hashMap);




                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    private void sendMessage(String sender, final String receiver, String message, String msg_time) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen", false);
        hashMap.put("msg_time", msg_time);

        reference.child("Chats").push().setValue(hashMap);


        //Adding Users to chat fragment: latest chats

        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(fuser.getUid())
                .child(userid);


        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userid);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        final String MSG = message;
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                if (notify) {
                    sendNotification(receiver, users.getUsername(), MSG);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Token token = snapshot1.getValue(Token.class);
                    Data data = null;
                    try {
                        data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username + ": " + decryptionMethod(message), "New Message", userid);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages(final String myid, final String userid, final String imagrurl) {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid) && chat.getSender().equals(userid) || chat.getReceiver().equals(userid) && chat.getSender().equals(myid)) {

                        mChat.add(chat);
                    }
                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imagrurl);
                    recyclerView.setAdapter(messageAdapter);
                    recyclerView.smoothScrollToPosition(mChat.size());

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }


    private void CheckStatus(String status) {
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        reference.updateChildren(hashMap);

    }

    private void LastSeenOffline() {
        String lastseen;
        String strDate;
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());
        Date date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            date = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yyyy");
            strDate = dateFormat.format(date);
        } else {
            strDate = "No Last Seen Available";
        }
        lastseen = strDate;
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastseen", "Last seen: " + lastseen);
        reference.updateChildren(hashMap);

    }

    private void LastSeenOnline() {
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastseen", "online");
        reference.updateChildren(hashMap);

    }


    @Override
    protected void onResume() {
        super.onResume();
        CheckStatus("online");
        LastSeenOnline();
        currentUser(userid);


    }

    @Override
    protected void onPause() {
        super.onPause();

        reference.removeEventListener(seenListener);

        CheckStatus("offline");
        LastSeenOffline();

        currentUser("none");


    }

    private String encryptionMethod(String string){

        byte[] stringByte=string.getBytes();
        byte[] encryptedByte=new byte[stringByte.length];
        try {
            cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec);
            encryptedByte=cipher.doFinal(stringByte);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        String encryptedOutput=null;
        try {

            encryptedOutput = new String(encryptedByte,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return encryptedOutput;

    }



    private String decryptionMethod(String string) throws UnsupportedEncodingException {

        byte[] EncryptedByte=string.getBytes("ISO-8859-1");
        String decryptedString=string;
        byte[] decryption;
        try {
            decipher.init(cipher.DECRYPT_MODE,secretKeySpec);
            decryption=decipher.doFinal(EncryptedByte);
            decryptedString= new String(decryption);

        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;

    }






}


