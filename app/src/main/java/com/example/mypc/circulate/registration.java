package com.example.mypc.circulate;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.collection.LLRBNode;

public class registration extends AppCompatActivity {

    TextInputEditText usernameEdittext,pass,confpass,emailEditText;
    Button register;
    String username,password,userEmailid;
    ProgressDialog registrationDialog;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        username=getIntent().getStringExtra("username");
        password=getIntent().getStringExtra("password");


        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if(actionBar!=null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#112d31")));
            actionBar.setElevation(0);
        }

        register=(Button)findViewById(R.id.registerButton);
        usernameEdittext=(TextInputEditText)findViewById(R.id.usernameInp);
        pass=(TextInputEditText)findViewById(R.id.passwordInp);
        confpass=(TextInputEditText)findViewById(R.id.confirmpasswordInp);
        emailEditText=(TextInputEditText)findViewById(R.id.emailFieldBox);

        auth=FirebaseAuth.getInstance();
        registrationDialog=new ProgressDialog(this);

        usernameEdittext.setText(username);
        pass.setText(password);

    }

    public void register(View view){
        String email=" ";
        try {
            if (!(confpass.getText().toString().equals(pass.getText().toString()))) {
                Toast.makeText(getApplicationContext(), "Password doesn't match!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pass.getText().toString().length() < 6) {
                Toast.makeText(getApplicationContext(), "Minimum length of password is 6", Toast.LENGTH_SHORT).show();
                return;
            }
            password = pass.getText().toString();
            email = emailEditText.getText().toString();
        }
        catch (NullPointerException e){
            Toast.makeText(getApplicationContext(),"Please enter all the fields above",Toast.LENGTH_SHORT).show();
            return;
        }
        if(email.trim().isEmpty() || password.trim().isEmpty()){
            Toast.makeText(getApplicationContext(),"Please enter all the fields above",Toast.LENGTH_SHORT).show();
            return;
        }
        //VERIFY EMAIL PATTERN
        registrationDialog.setTitle("Registering..");
        registrationDialog.setMessage("Please wait");
        registrationDialog.show();


        if(!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                registrationDialog.dismiss();
                                updateProfile();
                            } else {
                                try{
                                   throw task.getException();
                                }
                                catch (FirebaseAuthUserCollisionException e){
                                    Toast.makeText(registration.this,"Email id is already in use",Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e) {
                                    Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                                }
                                registrationDialog.dismiss();
                            }
                        }
                    });
        }
        else{
            Toast.makeText(getApplicationContext(),"Wrong email id.",Toast.LENGTH_SHORT).show();
            registrationDialog.dismiss();
        }
    }
    /* MAKE CHANGES TO ABOVE METHOD AND SIGNIN METHOD IN PREVIOUs ACTIVITY*/

    public void updateProfile(){
        final FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        UserProfileChangeRequest profileChangeRequest=new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileChangeRequest)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),"Registration successful!",Toast.LENGTH_SHORT).show();
                            FirebaseDatabase database=FirebaseDatabase.getInstance();
                            database.getReference("users").child(username).setValue(emailEditText.getText().toString());
                            finish();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getApplicationContext(),sign_in.class));
                        }
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(),"Some error occured. Please try again.",Toast.LENGTH_SHORT).show();
                user.delete();
            }
        });
    }
}
