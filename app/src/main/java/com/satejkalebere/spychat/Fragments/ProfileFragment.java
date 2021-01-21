package com.satejkalebere.spychat.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.satejkalebere.spychat.EditProfile;
import com.satejkalebere.spychat.Model.Users;
import com.satejkalebere.spychat.R;


public class ProfileFragment extends Fragment {


    TextView username, about;
    ImageView imageView;


    DatabaseReference reference;
    FirebaseUser fuser;


    //Profile Image
    StorageReference storageReference;

    Button editProfileButton;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        imageView = view.findViewById(R.id.myprofile_image2);
        username = view.findViewById(R.id.myusername);
        editProfileButton = view.findViewById(R.id.EditProfile);
        about = view.findViewById(R.id.about);


        //Profile image reference in storage
        storageReference = FirebaseStorage.getInstance().getReference("uploads");







        fuser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());


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

                if (user.getImageURL().equals("default")) {
                    imageView.setImageResource(R.drawable.ic_baseline_account_circle_24_white);

                } else {
                    try {
                        Glide.with(getContext()).load(user.getImageURL()).into(imageView);
                    } catch (Exception e) {

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                FirebaseCrash.report(error.toException());


            }
        });


        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), EditProfile.class);
                startActivity(i);
            }
        });





        return view;
    }


}