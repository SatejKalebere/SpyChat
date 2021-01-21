package com.satejkalebere.spychat;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.satejkalebere.spychat.Model.Users;

public class ViewUserProfile extends AppCompatActivity {

    TextView username, about;

    ImageView imageView;
    TextView lastseen;


    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;


    String userid;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);


        //widgets

        username = findViewById(R.id.userusername);
        lastseen = findViewById(R.id.userlastseen);
        imageView = findViewById(R.id.userProfilepic);
        about = findViewById(R.id.userabout);


        intent = getIntent();
        userid = intent.getStringExtra("userid");


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);

                username.setText(user.getUsername());
                if (user.getAbout().isEmpty()) {
                    about.setVisibility(View.GONE);
                } else {
                    about.setText(user.getAbout());
                    about.setVisibility(View.VISIBLE);
                }
                if (user.getLastseen().equals("none")) {
                    lastseen.setVisibility(View.GONE);
                } else if (user.getLastseen().equals("Last seen: No Last Seen")) {
                    lastseen.setVisibility(View.GONE);
                } else {
                    lastseen.setText(user.getLastseen());
                    lastseen.setVisibility(View.VISIBLE);
                }


                if (user.getImageURL().equals("default")) {
                    imageView.setImageResource(R.drawable.ic_baseline_account_circle_24);
                } else {
                    try {
                        Glide.with(ViewUserProfile.this)
                                .load(user.getImageURL())
                                .into(imageView);
                    } catch (Exception e) {

                        Toast.makeText(ViewUserProfile.this, "Loading...", Toast.LENGTH_SHORT).show();
                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.dones:
                onBackPressed();

                return true;


        }
        return false;
    }
}