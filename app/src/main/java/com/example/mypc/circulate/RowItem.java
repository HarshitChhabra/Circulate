package com.example.mypc.circulate;

import android.media.Image;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class RowItem {

    private String fileName;
    private String timestamp;
    private String downloadUrl;
    private String note;
    private String date;
    private String ext;
    private ArrayList<String> recipients;

    public RowItem(){ }

    public RowItem(String fileName,String timestamp,String downloadUrl,String note,String date,String ext)
    {
        this.fileName=fileName;
        this.timestamp=timestamp;
        //downloadButton=new ImageButton()
        this.downloadUrl=downloadUrl;
        this.note=note;
        this.date=date;
        recipients=null;
        //this.reference=reference;
        this.ext=ext;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName){
        this.fileName=fileName;
    }
    public void setTimestamp(String timestamp){
        this.timestamp=timestamp;
    }
    public void setRecipients(ArrayList<String> recipients){
        this.recipients=new ArrayList<String>(recipients);
    }

    public String getTimestamp(){
        return timestamp;
    }
    //public void setDownloadUrl(String downloadUrl){ this.downloadUrl=downloadUrl; }
    public String getDownloadUrl(){ return downloadUrl;}
    public String getNote(){ return note; }
    public String getDate(){ return date; }
    public ArrayList<String> getRecipients(){ return recipients; }

    public String getExt() { return ext; }
}
