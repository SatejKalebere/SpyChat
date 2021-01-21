package com.satejkalebere.spychat.Fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.satejkalebere.spychat.Adapters.UserAdapter;
import com.satejkalebere.spychat.Model.Users;
import com.satejkalebere.spychat.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<Users> mUsers;

    EditText search_users;

    ImageButton clrBtn;


    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();


        mUsers.clear();


//       try {
//            ReadUsers();
//        }catch (Exception e){
//            Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
//        }


        clrBtn = view.findViewById(R.id.clreaBtn);
        clrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_users.setText("");
                mUsers.clear();
            }
        });


        search_users = view.findViewById(R.id.search_user);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mUsers.clear();
                searchUsers(search_users.getText().toString().toLowerCase());


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                searchUsers(s.toString().toLowerCase());


            }

            @Override
            public void afterTextChanged(Editable s) {

                searchUsers(search_users.getText().toString().toLowerCase());


            }
        });


        return view;

    }


    private void searchUsers(String s) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = FirebaseDatabase.getInstance().getReference("MyUsers").orderByChild("search").startAt(s).endAt(s + "\uf8ff");


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                if (search_users.getText().toString().equals("")) {
                    mUsers.clear();


                } else {

                    mUsers.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {


                        Users users = dataSnapshot.getValue(Users.class);
                        assert users != null;
                        assert firebaseUser != null;
                        if (!users.getId().equals(firebaseUser.getUid())) {

                            mUsers.add(users);


                        }


                    }


                    userAdapter = new UserAdapter(getContext(), mUsers, false);
                    recyclerView.setAdapter(userAdapter);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        //search_users.setText("");

        searchUsers(search_users.getText().toString().toLowerCase());
    }


//    private void ReadUsers(){
//        final FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
//        DatabaseReference reference= (DatabaseReference) FirebaseDatabase.getInstance().getReference("MyUsers");
//
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                if (search_users.getText().toString().equals("")) {
//                    mUsers.clear();
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Users user = snapshot.getValue(Users.class);
//                        assert user != null;
//                        if (!user.getId().equals(firebaseUser.getUid())) {
//                            mUsers.add(user);
//                        }
//
//                        userAdapter = new UserAdapter(getContext(), mUsers, false);
//                        recyclerView.setAdapter(userAdapter);
//                    }
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }


}