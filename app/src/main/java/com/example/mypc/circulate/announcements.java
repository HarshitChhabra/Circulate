package com.example.mypc.circulate;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;


public class announcements extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToogle;
    private FirebaseDatabase database=FirebaseDatabase.getInstance();
    private RecyclerView recyclerView;
    private String[] recipients;
    private boolean[] selectedRecipientsIndexes;
    private String title,description,annDate,annTime;
    private DatabaseReference branchDb;
    View newAnnouncementView,custUserDialogView,reportIssueDialogView;
    private AlertDialog addCustomUserDialog;
    final int addCustUser=100;
    private NavigationView navigationView;
    private ArrayList<String> selectedRecipients=new ArrayList<>();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcements);

        progressDialog=new ProgressDialog(announcements.this);
        progressDialog.setTitle("Loading announcements");
        progressDialog.setMessage("Please wait..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        drawerLayout=(DrawerLayout) findViewById(R.id.drawer);
        recyclerView=(RecyclerView) findViewById(R.id.announcementRecyclerView);
        drawerToogle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(drawerToogle);
        drawerToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView=(NavigationView)findViewById(R.id.navigationMenu);
        navigationView.setCheckedItem(R.id.announcementsMenuOpt);
        if (circulate_main.mode != null && circulate_main.mode.equals("student")) {
            navigationView.getMenu().removeItem(R.id.addCustomNav);
        }

        View header=navigationView.getHeaderView(0);
        TextView temp=(TextView) header.findViewById(R.id.navHeaderUsername);
        temp.setText(circulate_main.username);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        final ArrayList<announcement_item> annoucementList=new ArrayList<>();
        final announcement_adapter adapter=new announcement_adapter(this,annoucementList);
        recyclerView.setAdapter(adapter);


        branchDb=database.getReference(circulate_main.branch);
        //SET LISTENER

        branchDb.child(circulate_main.listenerVal).child("announcements")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressDialog.dismiss();
                    }
                });

        branchDb.child(circulate_main.listenerVal).child("announcements")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                        annoucementList.add(0,dataSnapshot.getValue(announcement_item.class));
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

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.circularsMenuOpt){
                    Intent intent=new Intent(getApplicationContext(),circulate_main.class);
                    startActivity(intent);
                }
                else if(item.getItemId()==R.id.signoutNav){
                    FirebaseAuth.getInstance().signOut();
                    Toast.makeText(getApplicationContext(),"Signed out successfully",Toast.LENGTH_SHORT).show();
                    finish();
                    startActivity(new Intent(getApplicationContext(),sign_in.class));
                }
                else if(item.getItemId()==R.id.addCustomNav){
                    drawerLayout.closeDrawers();
                    openCustomUserAlert();
                }
                else{
                    drawerLayout.closeDrawers();
                }
                return true;
            }
        });
        FloatingActionButton addannouncement=(FloatingActionButton)findViewById(R.id.addAnnouncement);
        if(!circulate_main.mode.equals("admin"))
            addannouncement.hide();
        addannouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Pop up to add circular
                AlertDialog.Builder builder=new AlertDialog.Builder(announcements.this);
                LayoutInflater inflater=(LayoutInflater)announcements.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                newAnnouncementView=inflater.inflate(R.layout.add_announcement,null);
                builder.setCancelable(false);
                builder.setTitle("Post an announcement");
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                        .setPositiveButton("Next",null);
                builder.setView(newAnnouncementView);
                AlertDialog dialog=builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        Button button=((AlertDialog)dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                title=((EditText)newAnnouncementView.findViewById(R.id.newAnnTitle)).getText().toString();
                                description=((EditText)newAnnouncementView.findViewById(R.id.newAnnDescription)).getText().toString();
                                if(!title.trim().isEmpty()&&!description.trim().isEmpty()){
                                    dialog.dismiss();
                                    setRecipients();
                                    recipientSelection();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"The above fields cannot be empty",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                dialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(drawerToogle.onOptionsItemSelected(item)){
            return true;
        }
        if(item.getItemId()==R.id.appHelp){ }
        else if(item.getItemId()==R.id.reportBugOpt){
            openReportBugDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openReportBugDialog(){
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(announcements.this);
        builder.setCancelable(false);
        builder.setTitle("Report an Issue");
        LayoutInflater inflater=(LayoutInflater) announcements.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        reportIssueDialogView=inflater.inflate(R.layout.report_issue_dialog,null);
        builder.setPositiveButton("Report", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(reportIssueDialogView);
        final android.app.AlertDialog dialog=builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((android.app.AlertDialog) dialog).getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String sub=((EditText)reportIssueDialogView.findViewById(R.id.subjectEditText)).getText().toString();
                        String description=((EditText)reportIssueDialogView.findViewById(R.id.descriptionEditText)).getText().toString();
                        if(!sub.trim().isEmpty() && !description.trim().isEmpty()) {
                            addBugReportToDB(sub, description);
                            dialog.dismiss();
                            Toast.makeText(announcements.this,"Issue Reported.",Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(announcements.this,"Enter valid information in above fields.",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }

    private void addBugReportToDB(String subject,String description){
        long time=System.currentTimeMillis();
        database.getReference("Bugs").child(String.valueOf(time)).child("Sender").setValue(circulate_main.username);
        database.getReference("Bugs").child(String.valueOf(time)).child("Subject").setValue(subject);
        database.getReference("Bugs").child(String.valueOf(time)).child("Description").setValue(description);
    }

    public void openCustomUserAlert(){
        AlertDialog.Builder builder=new AlertDialog.Builder(announcements.this);
        LayoutInflater inflater=(LayoutInflater) announcements.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        custUserDialogView=inflater.inflate(R.layout.adduser_dialog,null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                navigationView.setCheckedItem(R.id.announcementsMenuOpt);
            }
        })
                .setPositiveButton("Add",null);

        /*Note: Not using the above method to add listener to positive button as the dialog should be on the screen even if there's
         * invalid data entered in the dialog fields. Listener is added below. If not done this way and if the user enters invalid info
         * Toast is displayed but dialog also disappears.*/

        builder.setView(custUserDialogView);
        builder.setCancelable(false);
        addCustomUserDialog=builder.create();
        addCustomUserDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) addCustomUserDialog).getButton(android.app.AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        addCustomUser();
                    }
                });
            }
        });
        addCustomUserDialog.show();
    }

    public void addCustomUser(){
        EditText username=(EditText) custUserDialogView.findViewById(R.id.custRollNum);
        Spinner section=(Spinner) custUserDialogView.findViewById(R.id.custSection);
        Spinner year=(Spinner) custUserDialogView.findViewById(R.id.custYear);
        customUser user;

        if(username.getText().toString().trim().isEmpty()){
            Toast.makeText(getApplicationContext(),"Enter roll number",Toast.LENGTH_SHORT).show();
            return;
        }
        if(year.getSelectedItem().toString().equals("Select Year")){
            Toast.makeText(getApplicationContext(),"Select year",Toast.LENGTH_SHORT).show();
            return;
        }
        if(section.getSelectedItem().toString().equals("Select Section")){
            Toast.makeText(getApplicationContext(),"Select Section",Toast.LENGTH_SHORT).show();
            return;
        }
        if(!validateCustRollnum(username.getText().toString())){
            Toast.makeText(getApplicationContext(),"Wrong Roll number pattern",Toast.LENGTH_SHORT).show();
            return;
        }
        String studentBranch=FirebaseAuth.getInstance().getCurrentUser().getDisplayName().replace("admin","");
        user=new customUser(studentBranch,section.getSelectedItem().toString(),year.getSelectedItem().toString());
        addUserToDB(user,username.getText().toString());
    }

    public boolean validateCustRollnum(String rollnum){
        String rollnumPattern="1602-([1-9][1-9]-73[2-7]-[0-9][0-9][0-9])";
        return rollnum.matches(rollnumPattern);
    }

    public void addUserToDB(customUser user,String rno){
        //final customUser temp=user;
        //final String temprno=rno;
        database.getReference("Custom users").child(rno).setValue(user);
        Toast.makeText(getApplicationContext(),"Registration successful!",Toast.LENGTH_SHORT).show();
        addCustomUserDialog.dismiss();

    }


    private void recipientSelection(){
        final android.app.AlertDialog.Builder recipientBuilder=new android.app.AlertDialog.Builder(announcements.this);
        recipientBuilder.setTitle("Select recipients:");
        selectedRecipientsIndexes=new boolean[recipients.length];
        recipientBuilder.setMultiChoiceItems(recipients, selectedRecipientsIndexes, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                if(isChecked){
                    selectedRecipients.add(recipients[position]);
                }else{
                    selectedRecipients.remove(recipients[position]);
                }
            }
        });
        recipientBuilder.setCancelable(false);
        recipientBuilder.setPositiveButton("Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(selectedRecipients.size()==0){
                    Toast.makeText(getApplicationContext(),"No recipients selected.",Toast.LENGTH_SHORT).show();
                    return;
                }
                getCurrentTimeAndDate();
                announcement_item announcement=new announcement_item(title,description,annTime,annDate,selectedRecipients);
                for(int i=0;i<selectedRecipients.size();i++)
                    branchDb.child(selectedRecipients.get(i)).child("announcements").child(String.valueOf(System.currentTimeMillis())).setValue(announcement);
                branchDb.child("admin").child("announcements").child(String.valueOf(System.currentTimeMillis())).setValue(announcement);
                Toast.makeText(getApplicationContext(),"Annoucement Sent!",Toast.LENGTH_SHORT).show();
            }
        });
        recipientBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        recipientBuilder.setNeutralButton("Clear all", null);
        final android.app.AlertDialog recipientDialog=recipientBuilder.create();
        recipientDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button clearbtn=((android.app.AlertDialog) recipientDialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                clearbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListView list=((android.app.AlertDialog)recipientDialog).getListView();
                        for(int i=0;i<selectedRecipientsIndexes.length;i++){
                            selectedRecipientsIndexes[i]=false;
                            list.setItemChecked(i,false);
                        }
                        selectedRecipients.clear();
                        //recipientBuilder.notifyAll();
                    }
                });
            }
        });
        recipientDialog.show();
    }

    private void setRecipients(){
        if(circulate_main.branch.equals("civil") || circulate_main.branch.equals("eee")){
            recipients=new String[4];
            for(int i=1;i<=4;i++){
                recipients[i-1]=String.valueOf(i)+getNumberSuffix(i)+" Year "+circulate_main.branch.toUpperCase();
            }
        }
        else{
            recipients=new String[8];
            int itr=0;
            for(int i=1;i<=4;i++){
                recipients[itr]=String.valueOf(i)+getNumberSuffix(i)+" Year "+circulate_main.branch.toUpperCase()+" A";
                recipients[itr+1]=String.valueOf(i)+getNumberSuffix(i)+" Year "+circulate_main.branch.toUpperCase()+" B";
                itr+=2;
            }
        }
    }

    private String getNumberSuffix(int number){
        if(number==1)
            return "st";
        if(number==2)
            return "nd";
        if(number==3)
            return "rd";
        return "th";
    }
    private void getCurrentTimeAndDate() {
        Date date = new Date();
        String timePattern = "hh:mm a",datePattern="dd/MM/yy";
        DateFormat timeFormat = new SimpleDateFormat(timePattern);
        DateFormat dateFormat = new SimpleDateFormat(datePattern);
        annDate = dateFormat.format(date);
        annTime=timeFormat.format(date);
        //return dateFormat.format(date);
    }
}
