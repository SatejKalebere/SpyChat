package com.satejkalebere.spychat.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.satejkalebere.spychat.Adapters.UserAdapter;
import com.satejkalebere.spychat.Model.ChatList;
import com.satejkalebere.spychat.Model.Users;
import com.satejkalebere.spychat.Notifications.Token;
import com.satejkalebere.spychat.R;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private UserAdapter userAdapter;
    private List<Users> mUsers;

    FirebaseUser fuser;
    Query reference;
    private List<ChatList> usersList;

    RecyclerView recyclerView;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_chats,container,false);

        recyclerView=view.findViewById(R.id.recycler_view_recentchats);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        usersList=new ArrayList<>();


        reference= FirebaseDatabase.getInstance().getReference("ChatList").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();

                //Loop for all usrs
                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    ChatList chatList= dataSnapshot.getValue(ChatList.class);
                    usersList.add(chatList);


                }

                try{
                    chatList();
                }catch (Exception e){
                    Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        return view;


    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        try {
            reference.child(fuser.getUid()).setValue(token1);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
        }

    }


    private void chatList() {

        //Getting all recent chats
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    Users user = dataSnapshot.getValue(Users.class);
                    for (ChatList chatList : usersList) {

                        if (user.getId().equals(chatList.getId()) || chatList.getId().equals(user.getId())) {
                            try {
                                mUsers.add(user);
                            } catch (Exception e) {
                                Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
                            }


                        }
                    }


                }

                try {
                    userAdapter = new UserAdapter(getContext(), mUsers, true);
                    recyclerView.setAdapter(userAdapter);

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Loading...", Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }
}