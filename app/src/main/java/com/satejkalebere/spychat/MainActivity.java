package com.satejkalebere.spychat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.satejkalebere.spychat.Fragments.ChatsFragment;
import com.satejkalebere.spychat.Fragments.ProfileFragment;
import com.satejkalebere.spychat.Fragments.UsersFragment;
import com.satejkalebere.spychat.Model.Chat;
import com.satejkalebere.spychat.Model.Users;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    //Firebase
    FirebaseUser firebaseUser;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        myRef= FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Users users=dataSnapshot.getValue(Users.class);
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(MainActivity.this, "User Login: "+users.getUsername(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Tab Layout and view pager
        final TabLayout tabLayout = findViewById(R.id.tablayout);
        final ViewPager viewPager = findViewById(R.id.viewpager);


        myRef = FirebaseDatabase.getInstance().getReference("Chats");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unRead = 0;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReceiver().equals(firebaseUser.getUid()) && !chat.isIsseen()) {
                        unRead++;
                    }
                }
                if (unRead == 0) {
                    try {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    try {
                        viewPagerAdapter.addFragment(new ChatsFragment(), "Chats (" + unRead + ")");
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                    }

                }
                try {
                    viewPagerAdapter.addFragment(new UsersFragment(), "Users");
                    viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");
                    viewPager.setAdapter(viewPagerAdapter);
                    tabLayout.setupWithViewPager(viewPager);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    //Adding Logout Functionality

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                try {
                    CheckStatus("offline");
                    LastSeenOffline();
                }catch (Exception e){
                    Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                }

                return true;
            case R.id.about:

                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                try {
                    CheckStatus("online");
                    LastSeenOnline();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
                }

                return true;

        }
        return false;
    }

    private void CheckStatus(String status) {
        myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);
        myRef.updateChildren(hashMap);

    }

    private void LastSeenOffline() {
        String lastseen;
        String strDate;
        myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
        Date date = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            date = Calendar.getInstance().getTime();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a dd-MM-yyyy");
            strDate = dateFormat.format(date);
        } else {
            strDate = "No Last Seen";
        }
        lastseen = strDate;
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastseen", "Last seen: " + lastseen);
        myRef.updateChildren(hashMap);

    }

    private void LastSeenOnline() {
        myRef = FirebaseDatabase.getInstance().getReference("MyUsers").child(firebaseUser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("lastseen", "online");
        myRef.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            CheckStatus("online");
            LastSeenOnline();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();


        try {
            CheckStatus("offline");
            LastSeenOffline();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_SHORT).show();
        }


    }

    //Class ViewpagerAdapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();


        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }


        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }


    }
}