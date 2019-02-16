package com.example.mypc.circulate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;

public class sign_in extends AppCompatActivity {

    Intent nextWindow,signinWindow;
    FirebaseAuth auth;
    private String username,password;
    private final String extention="@circulate.com";
    EditText usernameEdittext,passwordEdittext;
    ProgressDialog signinProgress;
    String custbranch=null,custSection,custYear;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null) {
            String email = currentUser.getEmail();
            username=currentUser.getDisplayName();
            Log.i("usernameval",username);
            //username = new StringTokenizer(email, "@").nextToken();
            updateUI(currentUser);
        }
    }

    protected void updateUI(FirebaseUser user){
        if(user!=null) {
            Log.i("invokedcheck","invoked");
            if(username.contains("admin"))
                nextWindow.putExtra("mode","admin");
            else
                nextWindow.putExtra("mode","student");
            if(custbranch!=null){
                nextWindow.putExtra("branch",custbranch);
                nextWindow.putExtra("section",custSection);
                nextWindow.putExtra("year",custYear);
            }
            nextWindow.putExtra("username",username.toLowerCase());
            signinProgress.dismiss();
            finish();
            nextWindow.putExtra("intentname","signin");
            startActivity(nextWindow);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signinProgress=new ProgressDialog(this);
        nextWindow=new Intent(getApplicationContext(),circulate_main.class);
        signinWindow=new Intent(this,sign_in.class);
        auth=FirebaseAuth.getInstance();

        TextView forgotpasssView=(TextView)findViewById(R.id.forgotPassView);
        forgotpasssView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(sign_in.this,forgotPassword.class));
            }
        });

    }

    public boolean validateRollnum(String username){
        String rollnumPattern="1602-(1[56789]|20)-73[2-7]-(0[0-9][1-9]|0[1-9][0-9]|1[01][0-9]|120|30[1-9]|31[0-9]|32[0-4])";
        return username.matches(rollnumPattern);
    }

    public void getEmailId(View view){

        signinProgress.setCancelable(false);
        signinProgress.setTitle("Please wait..");
        signinProgress.setMessage("Signing in");

        InputMethodManager inputMethodMAnager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodMAnager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);


        usernameEdittext = (EditText) findViewById(R.id.username);
        passwordEdittext = (EditText) findViewById(R.id.password);
        username = usernameEdittext.getText().toString();
        password = passwordEdittext.getText().toString();

        if (username!=null && password!=null && !username.isEmpty() && !password.isEmpty()) {
            signinProgress.show();
            final FirebaseDatabase database=FirebaseDatabase.getInstance();
            database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(username)){
                        String email=dataSnapshot.child(username).getValue(String.class);
                        signIn(email);
                    }
                    else{
                        signinProgress.dismiss();
                        Toast.makeText(getApplicationContext(),"You are not registered with us. Kindly register.",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    signinProgress.dismiss();
                    Toast.makeText(getApplicationContext(),"Some error occured.Try again later.",Toast.LENGTH_SHORT).show();
                }
            });
        }


    }

    public void signIn(String email) {

        if (username!=null && password!=null && !username.isEmpty() && !password.isEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseDatabase.getInstance().getReference("Custom users")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for(DataSnapshot item:dataSnapshot.getChildren()){
                                                if(username.equals(item.getKey())) {
                                                    Log.i("checkingval", item.getKey());
                                                    customUser temp= item.getValue(customUser.class);
                                                    custbranch=temp.getBranch();
                                                    custSection=temp.getSection();
                                                    custYear=temp.getYear();
                                                }
                                            }
                                            Toast.makeText(getApplicationContext(),"Welcome",Toast.LENGTH_SHORT).show();
                                            FirebaseUser user = auth.getCurrentUser();
                                            updateUI(user);
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                // Sign in success, update UI with the signed-in user's information
                            } else {
                                // If sign in fails, display a message to the user.
                                try{
                                    throw task.getException();
                                }
                                catch(FirebaseAuthInvalidCredentialsException e){
                                    Toast.makeText(getApplicationContext(),"Invalid credentials",Toast.LENGTH_SHORT).show();
                                }
                                catch (FirebaseAuthInvalidUserException e){
                                    Toast.makeText(getApplicationContext(),"Account doesn't exist",Toast.LENGTH_SHORT).show();
                                }
                                catch (Exception e){
                                    Toast.makeText(getApplicationContext(),"Some error occured. Try again later",Toast.LENGTH_SHORT).show();
                                }
                                finally {
                                    signinProgress.dismiss();
                                    updateUI(null);
                                }
                            }
                        }
                    });

        }
        else{
            Toast.makeText(this,"No input",Toast.LENGTH_SHORT).show();
        }
    }
    public void register(View view){
        usernameEdittext = (EditText) findViewById(R.id.username);
        passwordEdittext = (EditText) findViewById(R.id.password);
        username = usernameEdittext.getText().toString();
        password = passwordEdittext.getText().toString();
        final ProgressDialog checking=new ProgressDialog(sign_in.this);
        checking.setCancelable(false);
        checking.setTitle("Loading");
        checking.setMessage("Please wait..");


        if (username!=null && password!=null && !username.isEmpty() && !password.isEmpty()) {
            if(password.length()<6){
                Toast.makeText(getApplicationContext(),"Minimum length of password is 6",Toast.LENGTH_SHORT).show();
                return;
            }
            if (!validateRollnum(username)) {
                FirebaseDatabase.getInstance().getReference("Custom users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!(dataSnapshot.hasChild(username))) {
                            Toast.makeText(sign_in.this, "Wrong roll number", Toast.LENGTH_SHORT).show();
                            //return;
                        }
                        else{
                            registrationPage(checking);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(sign_in.this,"Some error occurred. Try again later",Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else
                registrationPage(checking);
        }
        else{
            Toast.makeText(this,"No input",Toast.LENGTH_SHORT).show();
        }
    }
     void registrationPage(final ProgressDialog checking){
         InputMethodManager inputMethodMAnager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         if(inputMethodMAnager!=null)
             inputMethodMAnager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);

         checking.show();

         final FirebaseDatabase database=FirebaseDatabase.getInstance();
         database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if(dataSnapshot.hasChild(username)){
                     checking.dismiss();
                     Toast.makeText(sign_in.this,"You are already registered. Kindly login.",Toast.LENGTH_SHORT).show();
                 }
                 else{
                     Intent intent=new Intent(getApplicationContext(),registration.class);
                     intent.putExtra("username",username);
                     intent.putExtra("password",password);
                     checking.dismiss();
                     startActivity(intent);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 checking.dismiss();
                 Toast.makeText(getApplicationContext(),"Some error occurred.Try again later.",Toast.LENGTH_SHORT).show();
             }
         });
     }
}
