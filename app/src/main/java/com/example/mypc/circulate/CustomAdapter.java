package com.example.mypc.circulate;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class CustomAdapter extends ArrayAdapter<chat_message>{
    private Activity activity;
    private ArrayList<chat_message> messages;
    public int temp=0;

    public CustomAdapter(Activity context, int resource, ArrayList<chat_message> messages) {
        super(context, resource, messages);
        this.activity = context;
        this.messages = messages;
    }
    static class ViewHolder{
        TextView msgDisp,timeDisp,senderDisp;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=(LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int layoutResource;
        ViewHolder holder;
        Log.i("checkflags","updating view");
        chat_message msg=getItem(position);
        Log.i("usernamevalcheck", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        Log.i("usernamevalcheck2",FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        String current=FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if(msg!=null&& msg.getSender().equals(current.toLowerCase()))
            layoutResource=R.layout.right_msg_bubble;
        else {
            layoutResource = R.layout.left_msg_bubble;
            Log.i("Sender Receiver",msg.getSender()+" "+circulate_main.username+" "+msg.getMessage());
        }
        if(convertView==null){
            convertView=inflater.inflate(layoutResource,parent,false);
            holder=new ViewHolder();
            holder.msgDisp=convertView.findViewById(R.id.txt_msg);
            holder.timeDisp=convertView.findViewById(R.id.msgTimeTextView);
            holder.senderDisp=convertView.findViewById(R.id.senderTextView);
            convertView.setTag(holder);
        }
        else
            holder=(ViewHolder) convertView.getTag();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        holder.senderDisp.setText(msg.getSender());
        holder.senderDisp.measure(0,0);
        int senderwid=holder.senderDisp.getMeasuredWidth();
        holder.msgDisp.setMinWidth(senderwid);
        holder.msgDisp.setMaxWidth((int)(width*0.85));
        holder.msgDisp.setText(msg.getMessage());
        holder.timeDisp.setText(msg.getTime());
        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {

        chat_message msg=getItem(position);
        if(msg!=null)
            return msg.getSender().equals(circulate_main.username)?1:0;
        return position;
    }
}