package com.satejkalebere.spychat;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.satejkalebere.spychat.Model.Users;

import java.util.HashMap;

public class EditProfile extends AppCompatActivity {
    private static final int IMAGE_REQUEST = 1;
    TextView username;
    ImageView imageView;
    Button changePic, saveProfile;
    EditText about, username_edit;
    DatabaseReference reference;
    FirebaseUser fuser;
    //Profile Image
    StorageReference storageReference;
    private Uri imageUri;
    private StorageTask<UploadTask.TaskSnapshot> uploadTask;
    private String newUserName, newAbout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);


        imageView = findViewById(R.id.profilepic);
        username = findViewById(R.id.user__name);
        changePic = findViewById(R.id.ChangeProfilePic);
        about = findViewById(R.id.about_edit);
        username_edit = findViewById(R.id.username_edit);
        saveProfile = findViewById(R.id.saveProfile);


        //Profile image reference in storage
        storageReference = FirebaseStorage.getInstance().getReference("uploads");


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                username.setText(user.getUsername());

                about.setText(user.getAbout());
                username_edit.setText(user.getUsername());

                if (user.getImageURL().equals("default")) {
                    imageView.setImageResource(R.drawable.ic_baseline_account_circle_24_white);

                } else {
                    try {
                        Glide.with(EditProfile.this).load(user.getImageURL()).into(imageView);
                    } catch (Exception e) {

                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                FirebaseCrash.report(error.toException());


            }
        });


        saveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());


                if (username_edit.getText().toString().isEmpty() || username_edit.getText().toString().equals(username.getText().toString())) {
                    newUserName = username.getText().toString();
                } else {

                    newUserName = username_edit.getText().toString();

                }


                newAbout = about.getText().toString();


                HashMap<String, Object> saveProf = new HashMap<>();
                saveProf.put("about", newAbout);
                saveProf.put("username", newUserName);
                saveProf.put("search", newUserName.toLowerCase());
                reference.updateChildren(saveProf);
                onBackPressed();
            }
        });


        changePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


    }

    private void selectImage() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, IMAGE_REQUEST);

    }

    private String getFileExtention(Uri uri) {
        ContentResolver contentResolver = EditProfile.this.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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


    private void UploadMyImage() {
        final ProgressDialog progressDialog = new ProgressDialog(EditProfile.this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtention(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }


                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downLoadUri = task.getResult();

                        String mUri = downLoadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());


                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageURL", mUri);
                        reference.updateChildren(map);
                        reference.child("imageURL").setValue(mUri);
                        progressDialog.dismiss();


                    } else {
                        Toast.makeText(EditProfile.this, "Uploading Failed:(", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfile.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });

        } else {
            Toast.makeText(EditProfile.this, "No Image Selected!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(EditProfile.this, "Uploading...Please wait..", Toast.LENGTH_SHORT).show();
            } else {
                UploadMyImage();
            }


        }


    }


}