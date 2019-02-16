package com.example.mypc.circulate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class announcement_adapter extends RecyclerView.Adapter<announcement_adapter.announcement_adapter_holder> {

    private Context context;
    private int lastPosition=-1;
    private ArrayList<announcement_item> announcementArrayList;

    public announcement_adapter(Context context,ArrayList<announcement_item> announcementArrayList){
        this.context=context;
        this.announcementArrayList=announcementArrayList;
    }

    @Override
    public void onBindViewHolder(@NonNull announcement_adapter.announcement_adapter_holder holder, int position) {
        announcement_item announcement=announcementArrayList.get(position);

        holder.announcementTitle.setText(announcement.getTitle());
        holder.announcementDescription.setText(announcement.getDescription());
        holder.announcementTime.setText(announcement.getTime());
        holder.announcementDate.setText(announcement.getDate());
        final ArrayList<String> temp=announcement.getRecipients();
        if(circulate_main.mode.equals("admin")) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(context);
                    builder.setTitle("Recipients:");
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
        Animation animation= AnimationUtils.loadAnimation(context,R.anim.fade_in);
        holder.itemView.startAnimation(animation);
        lastPosition=position;
    }

    @NonNull
    @Override
    public announcement_adapter_holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.announcement_item_layout,parent,false);
        CardView card=(CardView) view.findViewById(R.id.cardViewItem);
        //card.setCardBackgroundColor(Color.RED);

        card.setRadius(30);
        return new announcement_adapter_holder(view);
    }

    @Override
    public int getItemCount() {
        return announcementArrayList.size();
    }

    class announcement_adapter_holder extends RecyclerView.ViewHolder{

        TextView announcementTitle,announcementDescription,announcementDate,announcementTime;
        public announcement_adapter_holder(View viewitem){
            super(viewitem);
            announcementTitle=viewitem.findViewById(R.id.announcementTitle);
            announcementDescription=viewitem.findViewById(R.id.announcementDescription);
            announcementDate=viewitem.findViewById(R.id.announcementDate);
            announcementTime=viewitem.findViewById(R.id.announcementTime);
        }
    }
}
