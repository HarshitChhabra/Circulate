package com.example.mypc.circulate;

public class chat_message {
    private String time,date,message,sender;
    public chat_message(){}
    public chat_message(String time,String date,String message,String sender){
        this.time=time;
        this.date=date;
        this.message=message;
        this.sender=sender;
    }

    public String getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }

    public String getSender() {
        return sender;
    }
}