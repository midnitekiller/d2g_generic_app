package com.direct2guests.d2g_tv.NonActivity;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.direct2guests.d2g_tv.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarException;

import static com.direct2guests.d2g_tv.NonActivity.Constant.ImgPath;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ServerUrl;

public class ChannelListAdapter extends ArrayAdapter<JSONObject> {

    int row_layout;
    ArrayList<JSONObject> list;
    Context context;
    String image,imgUrl,serveruri;
    int pos;

    public ChannelListAdapter(Context context, int row_layout, ArrayList<JSONObject> list, String serveruri) {
        super(context,row_layout, list);

        this.context=context;
        this.row_layout=row_layout;
        this.list=list;
        this.serveruri = serveruri;
    }

    public void setPosition(int pos){
        this.pos = pos;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(row_layout, parent, false);

        ImageView channel_logo = itemView.findViewById(R.id.channelLogo);
        TextView channel_name = itemView.findViewById(R.id.channelTitle);
        RelativeLayout channel_layout = itemView.findViewById(R.id.channelLayout);
        if(pos == position){
            channel_layout.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.listviewtint));
            channel_name.setTextColor(Color.parseColor("#000000"));
        }else{
            channel_layout.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.white));
            channel_name.setTextColor(Color.parseColor("#000000"));
        }

        try{
            if(list.get(position).has("channel_logo")){

                Picasso.with(context).load(serveruri + list.get(position).getString("img_path")).resize(120,120).into(channel_logo);
            }else{
                Picasso.with(context).load(R.drawable.not_available).resize(120,120).into(channel_logo);
            }
            String cname = list.get(position).getString("channel_name");
            channel_name.setText(cname);
        }catch(JSONException e){
            Log.d("JsonException : ","ChannelListAdapter Error");
            e.printStackTrace();
        }

        return itemView;
    }
}
