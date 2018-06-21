package com.direct2guests.d2g_tv.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.direct2guests.d2g_tv.Activities.ChatActivity.FrontDeskChatActivity;
import com.direct2guests.d2g_tv.NonActivity.NetworkConnection;
import com.direct2guests.d2g_tv.NonActivity.Variable;
import com.direct2guests.d2g_tv.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.direct2guests.d2g_tv.NonActivity.Constant.ApiBasePath;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ApiUrl;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ImgPath;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ServerUrl;
import static java.lang.Integer.parseInt;

public class FoodDrinksActivity extends Activity {
    private Variable vdata;
    private NetworkConnection nc = new NetworkConnection();
    public final static String RESTAURANT_ID = "com.direct2guests.d2g_tv.RESTAURANT_ID";
    public final static String RESTAURANT_NAME = "com.direct2guests.d2g_tv.RESTAURANT_NAME";
    public final static String RESTAURANT_DESC = "com.direct2guests.d2g_tv.RESTAURANT_DESC";
    public final static String RESTAURANT_IMAGE = "com.direct2guests.d2g_tv.RESTAURANT_IMAGE";
    public final static String RESTAURANT_COUNT = "com.direct2guests.d2g_tv.RESTAURANT_COUNT";
    public final static String PREFERENCE = "MY_PREFERENCE";
    public final static String CHATCOUNT = "CHAT_COUNT";
    public final static String LASTCOUNT = "LAST_COUNT";
    private TextView FDTitle;
    private ImageView HotelLogo;
    private LinearLayout RestaurantCardsParent;
    private Button HomeButton, BackButton;
    private FrameLayout[] RestaurantCard;
    private View child;
    private Intent i;

    private String[] restoID, restoName, restoOpen, restoClose, restoDesc, restoImage, time_open, time_close;
    private Calendar calendar;
    private String chour, cminutes, rescount;
    private int dbhour, dbminutes, openHr, openMin, closeHr, closeMin, timeO, timeC;

    private Tracker mTracker;
    // Font path
    String fontPathBoldRale = "raleway/Raleway_Bold.ttf";

    /* Chat Notification */
    private TextView notif_number;
    private Button notif_button;
    private JsonRequest jsonRequest;
    private String thread_url;
    private String[] msgs, frm;
    private JSONArray array;
    private JSONObject chatobj;
    private RequestQueue queue;
    private int lastCount, resCount, anotherCount;
    private Ringtone r;
    private Uri notification;
    private Thread t;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private BroadcastReceiver broadcast_reciever;
    private RelativeLayout chatNotiff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        setContentView(R.layout.activity_food_drinks);
        AnalyticsApplication application = (AnalyticsApplication) getApplicationContext();
        mTracker = application.getDefaultTracker();
        vdata = (Variable)getIntent().getSerializableExtra(Variable.EXTRA);
        rescount = getIntent().getStringExtra(FoodDrinksActivity.RESTAURANT_COUNT);
        calendar = Calendar.getInstance();
        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_APPEND);
        editor = sharedPreferences.edit();

        HotelLogo = findViewById(R.id.HotelLogoFD);
        RestaurantCardsParent = findViewById(R.id.FDLinear);
        HomeButton = findViewById(R.id.HomeButtonFD);
        BackButton = findViewById(R.id.backButtonFD);

        // Loading Font Face
        Typeface fontBoldRale = Typeface.createFromAsset(getAssets(), fontPathBoldRale);

        //TextView label
        FDTitle = findViewById(R.id.FDTitle);

        //Applying font
        FDTitle.setTypeface(fontBoldRale);

        notif_button = findViewById(R.id.chat_message);
        notif_number = findViewById(R.id.new_message);
        queue = Volley.newRequestQueue(this);

        chatNotiff = findViewById(R.id.notif);
//        Picasso.with(getApplicationContext()).load(vdata.getServerURL()+vdata.getHotelLogo()).into(HotelLogo);
        fooddrinksFocus();
    }
    @Override
    protected void onStart(){
        super.onStart();
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        broadcast_reciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish_fooddrinks")) {
                    //finishing the activity
                    finish();
                }
            }
        };
        registerReceiver(broadcast_reciever, new IntentFilter("finish_fooddrinks"));
        mTracker.setScreenName(vdata.getHotelName()+" ~ Room No. "+vdata.getRoomNumber()+" ~ "+"Food&Drinks View");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        loadRestaurants();
        if(rescount != null) {
            RestaurantCard[Integer.parseInt(rescount)].requestFocus();
        }
        if(sharedPreferences.contains(CHATCOUNT)){
            anotherCount = Integer.parseInt(sharedPreferences.getString(CHATCOUNT, ""));
        }
        if(sharedPreferences.contains(LASTCOUNT)){
            lastCount = Integer.parseInt(sharedPreferences.getString(LASTCOUNT, ""));
        }
        if(anotherCount <= 0) {
            notif_number.setVisibility(View.INVISIBLE);
        }else{
            notif_number.setText(Integer.toString(anotherCount));
            notif_number.setVisibility(View.VISIBLE);
        }
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {
                setNotif();
                chatNotiff.setVisibility(View.VISIBLE);
            }
        }
        notif_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notif_number.setVisibility(View.INVISIBLE);
                anotherCount = 0;
                editor.putString(CHATCOUNT, Integer.toString(anotherCount));
                editor.commit();
                t.interrupt();
                RestaurantCardsParent.removeAllViewsInLayout();
                Intent i = new Intent(FoodDrinksActivity.this, FrontDeskChatActivity.class);
                i.putExtra(Variable.EXTRA, vdata);
                i.putExtra(FrontDeskChatActivity.CHAT_FROM, "hotelservicesbutt");
                startActivity(i);
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        RestaurantCardsParent.removeAllViews();
        loadRestaurants();
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {
                setNotif();
                chatNotiff.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        /*Intent i = new Intent(this, HotelServicesActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        i.putExtra(HotelServicesActivity.CLICK_FROM, "food");
        startActivity(i);*/
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {
                t.interrupt();
            }
        }
        super.onBackPressed();
    }


    private void loadRestaurants(){

        if(vdata.getRestaurantID().length > 0) {
            restoID = vdata.getRestaurantID();
            restoName = vdata.getRestaurantName();
            restoOpen = vdata.getRestaurantOpen();
            restoClose = vdata.getRestaurantClose();
            restoDesc = vdata.getRestaurantDesc();
            restoImage = vdata.getRestaurantImage();
            RestaurantCard = new FrameLayout[restoID.length];
            ViewGroup.MarginLayoutParams params;
            chour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
            cminutes = Integer.toString(calendar.get(Calendar.MINUTE));
            for (int i = 0; i < restoID.length; i++) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                child = inflater.inflate(R.layout.food_drinks_restaurant_card, RestaurantCardsParent, false);
                RestaurantCard[i] = child.findViewById(R.id.restaurantCard);
                RestaurantCard[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        vdata.focusAnim(view, b, getApplicationContext(), FoodDrinksActivity.this);
                    }
                });
                if (i == 0) {
                    params = (ViewGroup.MarginLayoutParams) RestaurantCard[i].getLayoutParams();
                    params.setMargins(110, 0, 0, 0);
                    RestaurantCard[i].setLayoutParams(params);
                    RestaurantCard[i].requestFocus();
                } else if (i == restoID.length - 1) {
                    params = (ViewGroup.MarginLayoutParams) RestaurantCard[i].getLayoutParams();
                    params.setMargins(0, 0, 110, 0);
                    RestaurantCard[i].setLayoutParams(params);
                }

                ImageView resImage = child.findViewById(R.id.restoImage);
                TextView resName = child.findViewById(R.id.restoName);
                if (restoImage[i].equals("")) {
                    Picasso.with(getApplicationContext()).load(R.drawable.menus).fit().into(resImage);
                } else {
                    String image_url = vdata.getServerURL() + restoImage[i];
                    Picasso.with(getApplicationContext()).load(image_url).fit().into(resImage);
                }

                resName.setText(restoName[i]);


                RestaurantCard[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int index = Arrays.asList(RestaurantCard).indexOf(view);
                        Log.d("INDEX :", "card no." + index);
                        chour = Integer.toString(calendar.get(Calendar.HOUR_OF_DAY));
                        cminutes = Integer.toString(calendar.get(Calendar.MINUTE));
                        time_open = restoOpen[index].split(":");
                        time_close = restoClose[index].split(":");
                        openHr = Integer.parseInt(time_open[0]);
                        openMin = Integer.parseInt(time_open[1]);
                        closeHr = Integer.parseInt(time_close[0]);
                        closeMin = Integer.parseInt(time_close[1]);
                        if (chour.length() == 1) {
                            chour = "0" + chour;
                        }
                        if (cminutes.length() == 1) {
                            cminutes = "0" + cminutes;
                        }
                        dbhour = Integer.parseInt(chour);
                        dbminutes = Integer.parseInt(cminutes);
                        if (dbhour == openHr) {
                            if (dbminutes >= openMin) {
                                timeO = 1;
                            } else {
                                timeO = 0;
                            }
                        } else if (dbhour > openHr) {
                            timeO = 1;
                        } else {
                            timeO = 0;
                        }
                        Log.d("Time", "chour:" + chour + ":" + cminutes + "   ::  " + openHr + ":" + openMin + "    ::   " + closeHr + ":" + closeMin);
                        if (dbhour == closeHr) {
                            if (dbminutes <= closeMin) {
                                timeC = 1;
                            } else {
                                timeC = 0;
                            }
                        } else if (dbhour < closeHr) {
                            timeC = 1;
                        } else {
                            timeC = 0;
                        }
                        if (timeO == 1 && timeC == 1) {
                            cardClick(restoID[index], restoName[index], restoDesc[index], restoImage[index], index);
                        } else {
                            new AlertDialog.Builder(FoodDrinksActivity.this)
                                    .setTitle("Sorry We're Closed")
                                    .setMessage(restoName[index] + " is currently closed. Come back again in between " + restoOpen[index] + " - " + restoClose[index])
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int i) {
                                            dialog.dismiss();
                                            //start code hide status bar
                                            View decorView = getWindow().getDecorView();
                                            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                                            decorView.setSystemUiVisibility(uiOptions);
                                            //end code hide status bar

                                        }
                                    }).show();
                        }
                    }
                });
                RestaurantCardsParent.addView(child);
            }
        }

    }

    protected void fooddrinksFocus(){
        HomeButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusIcons(view,hasFocus,getApplicationContext(),FoodDrinksActivity.this);
            }
        });
        BackButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusIcons(view,hasFocus,getApplicationContext(),FoodDrinksActivity.this);
            }
        });



        HomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                t.interrupt();
                RestaurantCardsParent.removeAllViewsInLayout();
                Intent i = new Intent(FoodDrinksActivity.this, LauncherActivity.class);
                i.putExtra(Variable.EXTRA, vdata);
                startActivity(i);
                i = new Intent("finish_hotelservices");
                sendBroadcast(i);
                finish();
            }
        });
        BackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        notif_button.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusIcons(view,hasFocus,getApplicationContext(),FoodDrinksActivity.this);
            }
        });
    }

    private void cardClick(String restaurantID, String restaurantname, String restaurantdesc, String restaurantimage, int count){
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {

                t.interrupt();
            }
        }
        RestaurantCardsParent.removeAllViewsInLayout();
        i = new Intent(this, MenuLists.class);
        i.putExtra(Variable.EXTRA, vdata);
        i.putExtra(RESTAURANT_ID, restaurantID);
        i.putExtra(RESTAURANT_NAME, restaurantname);
        i.putExtra(RESTAURANT_DESC, restaurantdesc);
        i.putExtra(RESTAURANT_IMAGE, restaurantimage);
        i.putExtra(RESTAURANT_COUNT, Integer.toString(count));
        startActivity(i);

    }

    private void setNotif(){
        //if(chatNotiff.getVisibility() == View.VISIBLE) {
            thread_url = vdata.getApiUrl() + "chats.php?hotel_id=" + vdata.getHotelID() + "&guest_id=" + vdata.getGuestID();
            t = new Thread() {
                @Override
                public void run() {
                    try {
                        while (!isInterrupted()) {
                            Thread.sleep(6000);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    jsonRequest = new JsonArrayRequest(Request.Method.GET, thread_url, null,
                                            new Response.Listener<JSONArray>() {
                                                @Override
                                                public void onResponse(JSONArray response) {
                                                    try {
                                                        array = response;
                                                        if (array.length() > 0) {
                                                            msgs = new String[array.length()];
                                                            frm = new String[array.length()];
                                                            resCount = 0;
                                                            anotherCount = 0;
                                                            if (array != null) {
                                                                for (int i = 0; i < array.length(); i++) {
                                                                    chatobj = array.getJSONObject(i);
                                                                    if (chatobj.getString("status").equals("unseen") && chatobj.getString("msg_from").equals("admin")) {
                                                                        resCount++;
                                                                    }
                                                                }
                                                            }
                                                            Log.d("resCount :", Integer.toString(resCount));
                                                            if (lastCount < resCount) {
                                                                if (notif_number.getVisibility() == View.INVISIBLE) {
                                                                    notif_number.setVisibility(View.VISIBLE);
                                                                }
                                                                notif_number.setText(Integer.toString(lastCount + (resCount - lastCount)));
                                                                lastCount = resCount;
                                                                anotherCount = Integer.parseInt(notif_number.getText().toString());
                                                                editor.putString(CHATCOUNT, Integer.toString(anotherCount));
                                                                editor.putString(LASTCOUNT, Integer.toString(lastCount));
                                                                editor.commit();
                                                                try {
                                                                    notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                                                    r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                                                    r.play();
                                                                } catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }

                                                    } catch (JSONException e) {
                                                        Log.d("JSONException", "Unable to parse JSONArray");
                                                    }
                                                }
                                            },
                                            new Response.ErrorListener() {
                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                    Log.d("Error.Response", error.getLocalizedMessage());
                                                }
                                            }
                                    ) {
                                        @Override
                                        public Map<String, String> getHeaders() throws AuthFailureError {
                                            HashMap<String, String> params = new HashMap<String, String>();
                                            String creds = String.format("%s:%s", "api", "qwerty!@#123");
                                            String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                                            params.put("Authorization", auth);
                                            return params;
                                        }
                                    };
                                    queue.add(jsonRequest);

                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        //}
    }

}