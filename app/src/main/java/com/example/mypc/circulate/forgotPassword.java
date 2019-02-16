package com.example.mypc.circulate;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.StringTokenizer;

public class forgotPassword extends AppCompatActivity {

    private EditText rollnum;
    private Button getmailbtn;
    private TextView msg;
    ProgressDialog dialog;
    FirebaseDatabase database=FirebaseDatabase.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        rollnum=(EditText)findViewById(R.id.passEnterRoll);
        getmailbtn=(Button)findViewById(R.id.getResetMailBtn);
        msg=(TextView)findViewById(R.id.resetPassMsg);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#112d31")));
            actionBar.setElevation(0);
        }

        msg.setVisibility(View.GONE);

        getmailbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    InputMethodManager inputMethodMAnager=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodMAnager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                }
                catch (Exception e){}
                dialog=new ProgressDialog(forgotPassword.this);
                dialog.setTitle("Loading");
                dialog.setMessage("Please wait");
                dialog.setCancelable(false);
                dialog.show();
                if(getmailbtn.getText().toString().equals("Enter again")){
                    getmailbtn.setText("Get Reset Mail");
                    msg.setVisibility(View.GONE);
                    rollnum.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                    return;
                }
                if(getmailbtn.getText().toString().equals("Go back to signin page")){
                    dialog.dismiss();
                    startActivity(new Intent(getApplicationContext(),sign_in.class));
                    finish();
                }
                final String rollnumber=rollnum.getText().toString();
                if(!rollnumber.isEmpty() && validateRollnum(rollnumber)){
                    database.getReference("users").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(rollnumber)){
                                String email=dataSnapshot.child(rollnumber).getValue(String.class);
                                sendResetMail(email);
                            }
                            else{
                                msg.setText("No registration found. Kindly check your Roll-Number");
                                rollnum.setVisibility(View.GONE);
                                msg.setVisibility(View.VISIBLE);
                                getmailbtn.setText("Enter again");
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(),"Some error occured. Try again later.",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(),"Enter valid Roll-number",Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }
        });
    }
    public boolean validateRollnum(String username){
        String rollnumPattern="1602-(1[6789]|20)-73[2-7]-(0[0-9][1-9]|0[1-9][0-9]|1[01][0-9]|120|30[1-9]|31[0-9]|32[0-4])";
        return username.matches(rollnumPattern);
    }

    private void sendResetMail(final String emailid){
        FirebaseAuth.getInstance().sendPasswordResetEmail(emailid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            StringTokenizer s1=new StringTokenizer(emailid,"@");
                            char temp[]=s1.nextToken().toCharArray();
                            int templen=temp.length;
                            for(int i=0;i<=templen/2;i++)
                                temp[i]='*';
                            String res=new String(temp);
                            msg.setText("Password reset email succesfully sent to email id "+res+s1.nextToken());
                            rollnum.setVisibility(View.GONE);
                            msg.setVisibility(View.VISIBLE);
                            getmailbtn.setText("Go back to signin page");
                            dialog.dismiss();
                            //Toast.makeText(getApplicationContext(),"Password reset email sent",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(),"Some errro occured. Please try again later",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
