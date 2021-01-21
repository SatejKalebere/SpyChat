package com.satejkalebere.spychat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    EditText emailETLogin, passETLogin;
    Button LoginBtn, RegisterBtn, ResetPasswordBtn;

    //Firebase
    FirebaseAuth auth;

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Checking for users Existance

        if(firebaseUser!=null){
            Intent i= new Intent(LoginActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailETLogin = findViewById(R.id.userEmailLogin);
        passETLogin = findViewById(R.id.userPasswordLogin);
        LoginBtn = findViewById(R.id.LoginBTN);
        RegisterBtn = findViewById(R.id.RegisterOnLogin);
        ResetPasswordBtn = findViewById(R.id.Forgot_password);

        //Firebase Auth:
        auth = FirebaseAuth.getInstance();


        //Create Account Button
        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });


        //Reset Password Button
        ResetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(i);
            }
        });

        //Login Button:
        LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_text = emailETLogin.getText().toString();
                String pass_text = passETLogin.getText().toString();

                //Checking if it is empty
                if (TextUtils.isEmpty(email_text) || TextUtils.isEmpty(pass_text)) {
                    Toast.makeText(LoginActivity.this, "Enter Email And Password!!", Toast.LENGTH_SHORT).show();
                } else {
                    auth.signInWithEmailAndPassword(email_text, pass_text)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();
                                    }else {
                                        Toast.makeText(LoginActivity.this, "Email or Password is incorrect!!", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            });
                }
            }
        });

    }
}