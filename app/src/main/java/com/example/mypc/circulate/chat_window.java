package com.example.mypc.circulate;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class chat_window extends AppCompatActivity {

    private ArrayList<chat_message> allMessages;
    private FloatingActionButton sendBtn;
    private ArrayList<chat_message> messageArrayList;
    //private EditText messageEditText;
    private ListView messageListView;
    private FirebaseDatabase database=FirebaseDatabase.getInstance();
    private String circularName,fileUrl;
    private ProgressDialog dialog;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_window);

        dialog=new ProgressDialog(chat_window.this);
        dialog.setTitle("Loading messages");
        dialog.setMessage("Please wait..");
        dialog.setCancelable(false);
        dialog.show();

        //In XML, transcriptmode and stackfrombottom used to scroll down of the list view everytime
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //The above statement makes sure that keyboard doesn't popup when the activity loads
        Log.i("calling this","create");
        circularName=getIntent().getStringExtra("title");
        fileUrl=getIntent().getStringExtra("url");

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null)
            actionBar.setTitle(circularName);

        messageArrayList=new ArrayList<chat_message>();
        sendBtn=(FloatingActionButton)findViewById(R.id.sendButton);
        messageListView=(ListView)findViewById(R.id.messages);
        final CustomAdapter adapter=new CustomAdapter(this,0,messageArrayList);
        messageListView.setAdapter(adapter);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message=((EditText)findViewById(R.id.messageBox)).getText().toString();
                if(!message.trim().isEmpty()){
                    String time=getCurrentTimeUsingDate();
                    String date=getDate();
                    String currentUsername= FirebaseAuth.getInstance().getCurrentUser().getDisplayName().toLowerCase();
                    chat_message temp=new chat_message(time,date,message,currentUsername);
                    addMsgToDB(temp);
                    ((EditText)findViewById(R.id.messageBox)).setText("");
                }
            }
        });

        database.getReference(circulate_main.branch).child(circularName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        database.getReference(circulate_main.branch).child(circularName)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        chat_message msg=dataSnapshot.getValue(chat_message.class);
                        messageArrayList.add(msg);
                        adapter.notifyDataSetChanged();
                        
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
    }

    private void addMsgToDB(chat_message msg){
        long timeinmillis= Calendar.getInstance().getTimeInMillis();
        database.getReference(circulate_main.branch).child(circularName).child(String.valueOf(timeinmillis)).setValue(msg);
        Log.i("checkflags","uploaded");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.chat_window_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.circularDescription){
            Intent intent=new Intent(getApplicationContext(),circular_description.class);
            Intent currentintent=getIntent();
            intent.putExtra("title",circularName);
            intent.putExtra("cirtime",currentintent.getStringExtra("cirtime"));
            intent.putExtra("cirdate",currentintent.getStringExtra("cirdate"));
            intent.putExtra("url",fileUrl);
            intent.putExtra("note",currentintent.getStringExtra("note"));
            startActivity(intent);
        }
        else if(item.getItemId()==android.R.id.home){
            startActivity(new Intent(getApplicationContext(),circulate_main.class));
        }
        return true;
    }

    private static String getCurrentTimeUsingDate() {
        Date date = new Date();
        String timePattern = "hh:mm a",datePattern="dd/MM/yy";
        DateFormat dateFormat = new SimpleDateFormat(timePattern);
        return dateFormat.format(date);
    }

    private String getDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
        String formattedDate = df.format(c);
        return formattedDate;
    }

}