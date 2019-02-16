package com.example.mypc.circulate;

import android.animation.Animator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.inputmethodservice.Keyboard;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

public class circulate_main extends AppCompatActivity{

    private Button buttonChoose,buttonUpload,buttonDownload;
    final int addCustUser=100;
    private ImageButton pdfButton,wordButton,imageButton;
    private static final int PDF_REQUEST=1;
    private Uri filepath;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri downloadUrl=null;
    private String fileName=null,custBranch,custSec,custYear;
    public static String username,branch,listenerVal="",mode,fileType;
    private View dialogLayout,custUserDialogView,noteDialogView,reportIssueDialogView;
    private FloatingActionButton addCircular;
    private AlertDialog selectTypeDialog,addCustomUserDialog,noteDialog;
    private String[] recipients;
    private boolean[] selectedRecipientsIndexes;
    private ArrayList<String> selectedRecipients=new ArrayList<>();
    private FirebaseDatabase database=FirebaseDatabase.getInstance();
    private DatabaseReference branchDB;
    private ProgressDialog loadingCirculars;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToogle;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circulate_main);
        Log.i("checkcreate","created");

        drawerLayout=(DrawerLayout) findViewById(R.id.drawer);
        drawerToogle=new ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close);
        drawerLayout.addDrawerListener(drawerToogle);
        drawerToogle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView=(NavigationView)findViewById(R.id.navigationMenu);
        navigationView.setCheckedItem(R.id.circularsMenuOpt);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId()==R.id.announcementsMenuOpt){
                    Intent intent=new Intent(getApplicationContext(),announcements.class);
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

        loadingCirculars = new ProgressDialog(this);
        loadingCirculars.setCancelable(false);
        loadingCirculars.show();
        loadingCirculars.setTitle("Please wait..");
        loadingCirculars.setMessage("Loading circulars");

        mode = getIntent().getStringExtra("mode");
        username = getIntent().getStringExtra("username");
        Log.i("usernamevalfrommain",username);
        View header=navigationView.getHeaderView(0);
        TextView temp=(TextView) header.findViewById(R.id.navHeaderUsername);
        temp.setText(username);


        custBranch = getIntent().getStringExtra("branch");
        //Log.i("custbranch",custBranch);
        if (custBranch != null) {
            Log.i("checkingVals", custBranch);
            custSec = getIntent().getStringExtra("section");
            custYear = getIntent().getStringExtra("year");
        }
        addCircular = (FloatingActionButton) findViewById(R.id.addCircular);

        if (mode != null && mode.equals("student")) {
            addCircular.hide();
            navigationView.getMenu().removeItem(R.id.addCustomNav);
        }
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        setYearBranchSec();
        database.getReference(branch).child(listenerVal).child("circulars")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        ArrayList<RowItem> circulars = new ArrayList<>();
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            RowItem circular = item.getValue(RowItem.class);
                            circulars.add(0,circular);
                        }
                        populateView(circulars);
                        loadingCirculars.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        loadingCirculars.dismiss();
                    }
                });

        setRecipients();
        branchDB = database.getReference(branch);

        addCircular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(circulate_main.this);
                LayoutInflater inflater = (LayoutInflater) circulate_main.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                dialogLayout = inflater.inflate(R.layout.dialog_view, null);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setCancelable(false);
                setImageButtons();
                builder.setView(dialogLayout);
                selectTypeDialog = builder.create();
                selectTypeDialog.show();
            }
        });
    //}
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("checkflag","calledstart");
        navigationView.setCheckedItem(R.id.circularsMenuOpt);
        drawerLayout.closeDrawers();
        if(FirebaseAuth.getInstance().getCurrentUser()==null){
            Toast.makeText(getApplicationContext(),"Please sign in",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(),sign_in.class));
        }
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
        if(item.getItemId()==R.id.appHelp){
            /*FirebaseAuth.getInstance().signOut();
            Toast.makeText(getApplicationContext(),"Signed out successfully",Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(getApplicationContext(),sign_in.class));*/
        }
         else if(item.getItemId()==R.id.reportBugOpt){
            openReportBugDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openReportBugDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(circulate_main.this);
        builder.setCancelable(false);
        builder.setTitle("Report an Issue");
        LayoutInflater inflater=(LayoutInflater) circulate_main.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        reportIssueDialogView=inflater.inflate(R.layout.report_issue_dialog,null);
        builder.setPositiveButton("Report", null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setView(reportIssueDialogView);
        final AlertDialog dialog=builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String sub=((EditText)reportIssueDialogView.findViewById(R.id.subjectEditText)).getText().toString();
                        String description=((EditText)reportIssueDialogView.findViewById(R.id.descriptionEditText)).getText().toString();
                        if(!sub.trim().isEmpty() && !description.trim().isEmpty()) {
                            addBugReportToDB(sub, description);
                            dialog.dismiss();
                            Toast.makeText(circulate_main.this,"Issue Reported.",Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(circulate_main.this,"Enter valid information in above fields.",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        dialog.show();
    }

    private void addBugReportToDB(String subject,String description){
        long time=System.currentTimeMillis();
        database.getReference("Bugs").child(String.valueOf(time)).child("Sender").setValue(username);
        database.getReference("Bugs").child(String.valueOf(time)).child("Subject").setValue(subject);
        database.getReference("Bugs").child(String.valueOf(time)).child("Description").setValue(description);
    }

    public void addUserToDB(customUser user,String rno){
        database.getReference("Custom users").child(rno).setValue(user);
        Toast.makeText(getApplicationContext(),"Registration successful!",Toast.LENGTH_SHORT).show();
        addCustomUserDialog.dismiss();

    }

    public boolean validateCustRollnum(String rollnum){
        String rollnumPattern="1602-([1-9][1-9]-73[2-7]-[0-9][0-9][0-9])";
        return rollnum.matches(rollnumPattern);
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

    public void openCustomUserAlert(){
        AlertDialog.Builder builder=new AlertDialog.Builder(circulate_main.this);
        LayoutInflater inflater=(LayoutInflater) circulate_main.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        custUserDialogView=inflater.inflate(R.layout.adduser_dialog,null);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                navigationView.setCheckedItem(R.id.circularsMenuOpt);
                dialogInterface.dismiss();
            }
        })
        .setPositiveButton("Add",null) ;

        /*Note: Not using the above method to add listener to positive button as the dialog should be on the screen even if there's
        * invalid data entered in the dialog fields. Listener is added below. If not done this way and if the user enters invalid info
        * Toast is displayed but dialog also disappears.*/

        builder.setView(custUserDialogView);
        builder.setCancelable(false);
        addCustomUserDialog=builder.create();
        addCustomUserDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                navigationView.setCheckedItem(R.id.circularsMenuOpt);
                Button button = ((AlertDialog) addCustomUserDialog).getButton(AlertDialog.BUTTON_POSITIVE);
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

    static class ViewHolder{
        TextView fileName,timestamp,date;
        ImageButton downloadButton,noteButton;
    }

    private void populateView(final ArrayList<RowItem> circulars){
        final ListView circularList=(ListView)findViewById(R.id.circularList);
        ArrayAdapter<RowItem> adapter =new ArrayAdapter<RowItem>(this,0,circulars){

            @Override
            public int getItemViewType(int position) {
                RowItem circular= circulars.get(position);
                if(circular.getNote()==null || circular.getNote().isEmpty())
                    return 0;
                return 1;
                //return super.getItemViewType(position);
            }

            @Override
            public int getViewTypeCount() {
                return 2;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder=new ViewHolder();
                //LayoutInflater mInflateer=(LayoutInflater) getApplicationContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                if(convertView==null){
                    convertView=getLayoutInflater().inflate(R.layout.list_item,null,false);

                    holder.fileName=(TextView) convertView.findViewById(R.id.fileName);
                    holder.downloadButton=(ImageButton) convertView.findViewById(R.id.downloadButton);
                    holder.timestamp=(TextView) convertView.findViewById(R.id.timestamp);
                    holder.date=(TextView) convertView.findViewById(R.id.date);
                    holder.noteButton=(ImageButton) convertView.findViewById(R.id.noteButton);

                    convertView.setTag(holder);
                }
                else{
                    holder=(ViewHolder)convertView.getTag();
                }
                final RowItem circular= circulars.get(position);
                holder.fileName.setText(circular.getFileName());
                holder.timestamp.setText(circular.getTimestamp());
                holder.date.setText(circular.getDate());

               // new CharSequence(circular.getRecipients());
                if(mode.equals("admin")){
                    holder.fileName.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            AlertDialog.Builder builder=new AlertDialog.Builder(circulate_main.this);
                            builder.setTitle("Recipients:");
                            ArrayList<String> temp=circular.getRecipients();
                            String []recipientsArray=new String[temp.size()];
                            for(int i=0;i<temp.size();i++)
                                recipientsArray[i]=temp.get(i);
                            builder.setItems(recipientsArray, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) { }
                            });
                            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.setCancelable(false);
                            builder.create().show();
                            return true;
                        }
                    });
                }

                final String circularName=holder.fileName.getText().toString();
                holder.fileName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent=new Intent(getApplicationContext(),chat_window.class);
                        intent.putExtra("title",circularName);
                        intent.putExtra("cirtime",circular.getTimestamp());
                        intent.putExtra("cirdate",circular.getDate());
                        intent.putExtra("url",circular.getDownloadUrl());
                        if(circular.getNote()==null||circular.getNote().trim().isEmpty())
                            intent.putExtra("note","none");
                        else
                            intent.putExtra("note",circular.getNote());
                        startActivity(intent);
                    }
                });

                final String note=circular.getNote();
                if(note==null || note.isEmpty())
                    holder.noteButton.setVisibility(View.INVISIBLE);
                else{
                    holder.noteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            AlertDialog.Builder builder=new AlertDialog.Builder(circulate_main.this);
                            builder.setMessage(note);
                            builder.setCancelable(false);
                            builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.create().show();
                        }
                    });
                }
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent downloadIntent = new Intent(Intent.ACTION_VIEW);
                        downloadIntent.setData(Uri.parse(circular.getDownloadUrl()));
                        startActivity(downloadIntent);

                        /*StorageReference ref=FirebaseStorage.getInstance().getReference().child(circular.getFileName()+"."+circular.getExt());
                        try {

                            File rootPath = new File(Environment.getExternalStorageDirectory(), "Files_storage");
                            if(!rootPath.exists()) {
                                rootPath.mkdirs();
                            }
                            //File localfile1=File.
                            final File localfile = File.createTempFile("gotfiles", circular.getFileName()+"."+circular.getExt());
                            ref.getFile(localfile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(circulate_main.this,"Download Completed",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    StringWriter sw = new StringWriter();
                                    e.printStackTrace(new PrintWriter(sw));
                                    String exceptionAsString = sw.toString();
                                    Toast.makeText(getApplicationContext(), exceptionAsString, Toast.LENGTH_LONG).show();
                                    Log.i("goterrorcheck",exceptionAsString);
                                    //Toast.makeText(circulate_main.this,"Failed",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        catch (Exception e){
                            Toast.makeText(circulate_main.this,"error occured",Toast.LENGTH_SHORT).show();
                            Toast.makeText(circulate_main.this,e.getStackTrace().toString(),Toast.LENGTH_LONG).show();
                        }*/
                    }
                });
                Animation animation= AnimationUtils.loadAnimation(circulate_main.this,R.anim.fade_in);
                convertView.startAnimation(animation);
                return convertView;
            }
        };

        circularList.setAdapter(adapter);
    }

    private static String getCurrentTimeUsingDate() {
        Date date = new Date();
        String strDateFormat = "hh:mm a";
        //Log.i("checkCheck",new SimpleDateFormat("dd:mm:yy").format(date).toString());
        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
        //String formattedDate= dateFormat.format(date);
        return dateFormat.format(date);
    }

    private String getDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
        String formattedDate = df.format(c);
        return formattedDate;
    }

    private void setYearBranchSec(){
        String section;
        int yearval;
        if(mode.contains("admin")) {
            listenerVal="admin";
            branch = username.replace("admin", "");
            return;
        }
        if(custBranch==null) {
            StringTokenizer tokenizer = new StringTokenizer(username, "-");
            tokenizer.nextToken();
            String userJoinYear = tokenizer.nextToken();
            String branchNumber = tokenizer.nextToken();
            String rno = tokenizer.nextToken();

            String[] monthName = {"January", "February", "March", "April", "May", "June", "July",
                    "August", "September", "October", "November", "December"};

            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH);
            int currentYear = cal.get(Calendar.YEAR) % 100;
            yearval = currentYear - Integer.parseInt(userJoinYear);
            if (month > 4)
                yearval += 1;

            switch (branchNumber) {
                case "732":
                    branch = "civil";
                    break;
                case "733":
                    branch = "cse";
                    break;
                case "734":
                    branch = "eee";
                    break;
                case "735":
                    branch = "ece";
                    break;
                case "736":
                    branch = "mech";
                    break;
                case "737":
                    branch = "it";
                    break;
            }

            if (Integer.parseInt(rno) < 61 || (Integer.parseInt(rno) > 300 && Integer.parseInt(rno) < 313))
                section = "A";
            else
                section = "B";
            listenerVal=yearval+getNumberSuffix(yearval)+" Year "+branch.toUpperCase()+" "+section;
        }
        else{
            branch=custBranch.toLowerCase();
            listenerVal=custYear+" Year "+branch.toUpperCase()+" "+custSec;
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

    private void noteDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Note");
        LayoutInflater inflater=(LayoutInflater) circulate_main.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        noteDialogView=inflater.inflate(R.layout.note_dialog,null);
        builder.setView(noteDialogView);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        })
        .setPositiveButton("Next", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                recipientSelection();
            }
        });
        noteDialog=builder.create();
        noteDialog.show();
    }

    private void setRecipients(){
        if(branch.equals("civil") || branch.equals("eee")){
            recipients=new String[4];
            for(int i=1;i<=4;i++){
                recipients[i-1]=String.valueOf(i)+getNumberSuffix(i)+" Year "+branch.toUpperCase();
            }
        }
        else{
            recipients=new String[8];
            int itr=0;
            for(int i=1;i<=4;i++){
                recipients[itr]=String.valueOf(i)+getNumberSuffix(i)+" Year "+branch.toUpperCase()+" A";
                recipients[itr+1]=String.valueOf(i)+getNumberSuffix(i)+" Year "+branch.toUpperCase()+" B";
                itr+=2;
            }
        }
    }

    private void setImageButtons(){
        pdfButton=(ImageButton) dialogLayout.findViewById(R.id.pdfButton);
        wordButton=(ImageButton) dialogLayout.findViewById(R.id.wordButton);
        imageButton=(ImageButton) dialogLayout.findViewById(R.id.imageButton);
        pdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileType="application/pdf";
                showFileChooser();
            }
        });

        wordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileType="application/msword";
                showFileChooser();
            }
        });

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileType="image/*";
                showFileChooser();
            }
        });
    }

    private void uploadFile(){
        if(filepath!=null) {
            final ProgressDialog progressDialog=new ProgressDialog(this);
            progressDialog.setTitle("Uploading..");
            progressDialog.setCancelable(false);
            progressDialog.show();

            final StorageReference fileRef = storageReference.child(fileName);

            UploadTask uploadTask=fileRef.putFile(filepath);

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100.0* taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    progressDialog.setMessage((int)progress+"% Uploaded..");
                }
            });
            Task<Uri> urlTask=uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        downloadUrl=task.getResult();
                        Log.i("download",downloadUrl.toString());
                        StringTokenizer temp=new StringTokenizer(fileName,".");
                        String name=temp.nextToken();
                        String time=getCurrentTimeUsingDate();
                        String date=getDate();
                        EditText noteField=(EditText)(noteDialog.findViewById(R.id.noteField));
                        String note=noteField.getText().toString();
                        if(note.trim().isEmpty())
                            note="";

                        RowItem circular=new RowItem(name,time,downloadUrl.toString(),note,date,temp.nextToken());


                        for(int i=0;i<selectedRecipients.size();i++){
                            branchDB.child(selectedRecipients.get(i)).child("circulars").child(String.valueOf(System.currentTimeMillis())).setValue(circular);
                        }
                        circular.setRecipients(selectedRecipients);
                        branchDB.child("admin").child("circulars").child(String.valueOf(System.currentTimeMillis())).setValue(circular);
                        progressDialog.dismiss();
                    }
                    else{
                        //failure
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"Failed to upload",Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(getApplicationContext(),"No file selected",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

                filepath = data.getData();
                fileName = getFileName(filepath);
                String displayMessage = "Selected File: " + fileName;
                noteDialog();
            }
        }
        catch (Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Toast.makeText(getApplicationContext(), exceptionAsString, Toast.LENGTH_SHORT).show();

        }
    }

    private void recipientSelection(){
        final AlertDialog.Builder recipientBuilder=new AlertDialog.Builder(circulate_main.this);
        recipientBuilder.setTitle("Select recipients:");
        selectedRecipientsIndexes=new boolean[recipients.length];
        recipientBuilder.setMultiChoiceItems(recipients, selectedRecipientsIndexes, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                    if(isChecked){
                        selectedRecipients.add(recipients[position]);
                        Log.i("checkmulti","adding");
                    }else{
                        selectedRecipients.remove(recipients[position]);
                        Log.i("checkmulti","removing");
                    }
            }
        });
        recipientBuilder.setCancelable(false);
        recipientBuilder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                String selected=" ";
                for(int i=0;i<selectedRecipients.size();i++)
                    selected+=selectedRecipients.get(i)+",";
                if(selected.equals(" "))
                {
                    Toast.makeText(getApplicationContext(),"No recipients selected",Toast.LENGTH_SHORT).show();
                    return;
                }
                //selectedFile.setText(selected);
                //Upload url to database
                uploadFile();
            }
        });
        recipientBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        recipientBuilder.setNeutralButton("Clear all", null);


        final AlertDialog recipientDialog=recipientBuilder.create();

        recipientDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button clearbtn=((AlertDialog) recipientDialog).getButton(DialogInterface.BUTTON_NEUTRAL);
                clearbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListView list=((AlertDialog)recipientDialog).getListView();
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

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    private void showFileChooser(){
        try {
            Intent intent = new Intent();
            intent.setType(fileType);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            selectTypeDialog.dismiss();
            startActivityForResult(Intent.createChooser(intent, "Select the pdf"), PDF_REQUEST);
        }
        catch(Exception e){
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            Toast.makeText(getApplicationContext(), exceptionAsString, Toast.LENGTH_SHORT).show();
        }
    }
}
