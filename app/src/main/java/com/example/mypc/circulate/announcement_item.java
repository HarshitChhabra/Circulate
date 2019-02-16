package com.example.mypc.circulate;

import java.util.ArrayList;

public class announcement_item {
    private String description,time,date,title;
    private ArrayList<String> recipients;
    public announcement_item(){}
    public announcement_item(String title,String description,String time,String date,ArrayList<String> recipients){
        this.title=title;
        this.description=description;
        this.time=time;
        this.date=date;
        this.recipients=recipients;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<String> getRecipients() {
        return recipients;
    }
}
