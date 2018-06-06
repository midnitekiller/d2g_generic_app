package com.direct2guests.d2g_tv.NonActivity;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.direct2guests.d2g_tv.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class CarSpaCartListAdapter extends ArrayAdapter<JSONObject> {
    Variable vdata;
    int row_layout;
    ArrayList<JSONObject> list;
    Context context;
    String image,imgUrl, stype;
    int pos, button;

    Typeface fontPathRegRale;
    Typeface fontPathBoldRale;
    Typeface fontPathRegCav;
    Typeface fontPathBoldCav;

    public CarSpaCartListAdapter(Context context, Variable vdata,int row_layout, String type, ArrayList<JSONObject> list){

        super(context,row_layout, list);

        fontPathRegRale = Typeface.createFromAsset(context.getAssets(), "raleway/Raleway-Regular.ttf");
        fontPathBoldRale = Typeface.createFromAsset(context.getAssets(), "raleway/Raleway_Bold.ttf");
        fontPathRegCav = Typeface.createFromAsset(context.getAssets(), "fonts/CaviarDreams.ttf");
        fontPathBoldCav = Typeface.createFromAsset(context.getAssets(), "fonts/CaviarDreams_Bold.ttf");
        this.vdata=vdata;
        this.context=context;
        this.row_layout=row_layout;
        this.list=list;
        if(type.equals("restaurant")){
            stype = "menus";
        }else if(type.equals("services")){
            stype = "services";
        }
    }

    public void setPosition(int pos){
        this.pos = pos;
    }

    public void setButton(int button){
        this.button = button;
    }
    public JSONObject getItemJSONObject(){
        JSONObject data = new JSONObject();
        try{
            if(stype.equals("menus")) {
                data.put("menu_id", list.get(pos).getString("restomenu_ID"));
                data.put("qty", 1);
            }else if(stype.equals("services")){
                data.put("menu_id", list.get(pos).getString("serviceProd_ID"));
                data.put("qty", 1);
            }
        }catch(JSONException e){
            e.printStackTrace();
        }
        return data;
    }

    public View getView(final int position, View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);



        final View itemView = inflater.inflate(row_layout, parent, false);


        ImageView img=itemView.findViewById(R.id.thumbnail);
        TextView name=itemView.findViewById(R.id.Name);
        TextView price=itemView.findViewById(R.id.Price);
        TextView quantity = itemView.findViewById(R.id.quantityText);
        Button deduct = itemView.findViewById(R.id.deductButton);
        Button add = itemView.findViewById(R.id.addButton);
        Button delete = itemView.findViewById(R.id.deleteButton);

        name.setTypeface(fontPathBoldCav);
        price.setTypeface(fontPathBoldCav);
        quantity.setTypeface(fontPathBoldCav);


        /* ======================== OnFocusChangeListener ========================== */
        delete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.quantitybuttoncartfocus));
                    button = 2;
                }else{
                    view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.quantitybuttoncartblur));
                }
            }
        });
        deduct.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.quantitybuttoncartfocus));
                    button = 0;
                }else{
                    view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.quantitybuttoncartblur));
                }

            }
        });
        add.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.quantitybuttoncartfocus));
                    button = 1;
                }else{
                    view.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.quantitybuttoncartblur));
                }

            }
        });
        /* ======================================== End OnFocusChangeListener ================================ */

        /* ======================================== OnClickListener ========================================== */

        deduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView)parent).performItemClick(view,position,0);
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView)parent).performItemClick(view,position,0);
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ListView)parent).performItemClick(view,position,0);
            }
        });
        /* ======================================= End OnClickListener =========================================== */

        if(pos == position && button == 0){
            deduct.requestFocus();
        }else if(pos == position && button == 1){
            add.requestFocus();
        }else if(pos == position && button == 2){
            delete.requestFocus();
        }

        try{
            image = list.get(position).getString("img_path");
            if(image.equals(null) || image.isEmpty()){
                imgUrl = "";
                Log.d("Image", " :: " + image);
            }else{
                imgUrl = "http://dashboard.direct2guests.tv/" + image;
                Log.d("Image", imgUrl);
            }

        }catch (JSONException e){
            e.printStackTrace();
        }

        try {
            if(imgUrl.isEmpty() && stype.equals("services")){
                Picasso.with(context).load(R.drawable.rentals).resize(200,200).into(img);
            }else if(imgUrl.isEmpty() && stype.equals("menus")){
                Picasso.with(context).load(R.drawable.menus).resize(200,200).into(img);
            }else{
                Picasso.with(context).load(imgUrl).resize(200,200).into(img);
            }


            String php=vdata.getCurrency()+" ";
            price.setText(php+list.get(position).get("subtotal").toString());
            if(stype.equals("services")){
                name.setText(list.get(position).getString("serviceProdName"));
                quantity.setText(list.get(position).getString("quantity")+ list.get(pos).getString("serviceProdDuration"));

            }else if(stype.equals("menus")){
                name.setText(list.get(position).getString("menu_name"));
                quantity.setText(list.get(position).getString("quantity"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return itemView;
    }


}
