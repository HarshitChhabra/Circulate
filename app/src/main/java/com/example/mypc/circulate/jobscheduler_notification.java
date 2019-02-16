//package com.example.mypc.circulate;
//
//import android.app.IntentService;
//import android.app.Service;
//import android.app.job.JobParameters;
//import android.app.job.JobService;
//import android.content.Intent;
//import android.os.IBinder;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.widget.Toast;
//
//import com.firebase.client.FirebaseError;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//import com.firebase.client.Firebase;
//
//import java.util.ArrayList;
//
//public class jobscheduler_notification extends Service {
//
//
//    private String branch,listenerval;
//    private Firebase firebasetemp;
//    private ValueEventListener listenertemp;
//
//    @Override
//    public void onCreate() {
//
//        super.onCreate();
//        //firebasetemp=new Firebase("https://circulate-ad9d1.firebaseio.com/cse/");
//        FirebaseDatabase database=FirebaseDatabase.getInstance();
//
//        database.getReference("cse").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Toast.makeText(getApplicationContext(),"got",Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        return START_STICKY;
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//}
