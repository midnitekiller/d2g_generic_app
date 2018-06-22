package com.direct2guests.d2g_tv.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.direct2guests.d2g_tv.NonActivity.VolleyCallbackArray;
import com.direct2guests.d2g_tv.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.direct2guests.d2g_tv.NonActivity.Constant.ApiBasePath;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ApiUrl;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ImgPath;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ServerUrl;
import static java.lang.Integer.parseInt;

public class PlacesNearbyActivity extends Activity {
    private Variable vdata;
    private NetworkConnection nc = new NetworkConnection();

    private FrameLayout Activities, Pubs, Restaurants;
    private Button HomeButton, BackButton;
    private TextView placeTextView, ActivitiesTextView, PubsTextView, RestaurantsTextView;
    public final static String PLACES_TYPE = "com.direct2guests.d2g_tv.PLACES_TYPE";
    private String pt;

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
    public final static String PREFERENCE = "MY_PREFERENCE";
    public final static String CHATCOUNT = "CHAT_COUNT";
    public final static String LASTCOUNT = "LAST_COUNT";
    private BroadcastReceiver broadcast_reciever;
    private RelativeLayout chatNotiff;

    // Font path
    String fontPathRegRale = "raleway/Raleway-Regular.ttf";
    String fontPathBoldRale = "raleway/Raleway_Bold.ttf";
    String fontPathRegCav = "fonts/CaviarDreams.ttf";
    String fontPathBoldCav = "fonts/CaviarDreams_Bold.ttf";

    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        setContentView(R.layout.activity_places_nearby);
        AnalyticsApplication application = (AnalyticsApplication) getApplicationContext();
        mTracker = application.getDefaultTracker();

        vdata = (Variable)getIntent().getSerializableExtra(Variable.EXTRA);
        pt = getIntent().getStringExtra(PlacesNearbyActivity.PLACES_TYPE);
        ImageView hotelimg_view = findViewById(R.id.HotelLogoImageView);
        Activities = findViewById(R.id.ActivitiesCard);
        Pubs = findViewById(R.id.PubsCard);
        Restaurants = findViewById(R.id.RestaurantsCard);

        HomeButton = findViewById(R.id.HomeButton);
        BackButton = findViewById(R.id.BackButton);

        sharedPreferences = getSharedPreferences(PREFERENCE, Context.MODE_APPEND);
        editor = sharedPreferences.edit();
        notif_button = findViewById(R.id.chat_message);
        notif_number = findViewById(R.id.new_message);
        queue = Volley.newRequestQueue(this);

        chatNotiff = findViewById(R.id.notif);
        // Loading Font Face
        Typeface fontRegRale = Typeface.createFromAsset(getAssets(), fontPathRegRale);
        Typeface fontBoldRale = Typeface.createFromAsset(getAssets(), fontPathBoldRale);

        // text view label
        placeTextView = findViewById(R.id.placeTextView);
        ActivitiesTextView = findViewById(R.id.ActivitiesTextView);
        PubsTextView = findViewById(R.id.PubsTextView);
        RestaurantsTextView = findViewById(R.id.RestaurantsTextView);

        //Applying font
        placeTextView.setTypeface(fontBoldRale);
        ActivitiesTextView.setTypeface(fontRegRale);
        PubsTextView.setTypeface(fontRegRale);
        RestaurantsTextView.setTypeface(fontRegRale);

//        Picasso.with(getApplicationContext()).load(vdata.getServerURL()+vdata.getHotelLogo()).into(hotelimg_view);
        onFocusFrames();
        Activities.requestFocus();
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
                if (action.equals("finish_placesnearby")) {
                    //finishing the activity
                    finish();
                }
            }
        };
        registerReceiver(broadcast_reciever, new IntentFilter("finish_placesnearby"));
        mTracker.setScreenName(vdata.getHotelName()+" ~ Room No. "+vdata.getRoomNumber()+" ~ "+"Places Nearbysss View");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        if(pt != null){
            setFocus(pt);
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
                Intent i = new Intent(PlacesNearbyActivity.this, FrontDeskChatActivity.class);
                i.putExtra(Variable.EXTRA, vdata);
                i.putExtra(FrontDeskChatActivity.CHAT_FROM, "hotelservicesbutt");
                startActivity(i);
            }
        });
        notif_button.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusIcons(view,hasFocus,getApplicationContext(),PlacesNearbyActivity.this);
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

        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {
                setNotif();
                chatNotiff.setVisibility(View.VISIBLE);
            }
        }
    }
    @Override
    public void onBackPressed(){
        /*Intent i = new Intent(this, HotelServicesActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        i.putExtra(HotelServicesActivity.CLICK_FROM, "places");
        startActivity(i);*/
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {

                t.interrupt();
            }
        }
        super.onBackPressed();
    }

    private void onFocusFrames(){
        Activities.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusAnim(view,hasFocus,getApplicationContext(),PlacesNearbyActivity.this);
            }
        });
        Pubs.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusAnim(view,hasFocus,getApplicationContext(),PlacesNearbyActivity.this);
            }
        });
        Restaurants.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusAnim(view,hasFocus,getApplicationContext(),PlacesNearbyActivity.this);
            }
        });

        HomeButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusIcons(view,hasFocus,getApplicationContext(),PlacesNearbyActivity.this);
            }
        });

        BackButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusIcons(view,hasFocus,getApplicationContext(),PlacesNearbyActivity.this);
            }
        });

        HomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < vdata.getHotelAccess().length; i++) {
                    if (vdata.getHotelAccess()[i].equals("chat_acc")) {
                        t.interrupt();
                    }
                }
                Intent i = new Intent(PlacesNearbyActivity.this, LauncherActivity.class);
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


    }

    public void ActivitiesDetails(View view){
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {

                t.interrupt();
            }
        }
        Intent i = new Intent(this, PlacesNearbyDetailsActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        i.putExtra(PLACES_TYPE, "Activities");
        startActivity(i);
    }

    public void PubsDetails(View view){
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {

                t.interrupt();
            }
        }
        Intent i = new Intent(this, PlacesNearbyDetailsActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        i.putExtra(PLACES_TYPE, "Nightlife");
        startActivity(i);
    }

    public void RestaurantsDetails(View view){
        for (int i = 0; i < vdata.getHotelAccess().length; i++) {
            if (vdata.getHotelAccess()[i].equals("chat_acc")) {

                t.interrupt();
            }
        }
        Intent i = new Intent(this, PlacesNearbyDetailsActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        i.putExtra(PLACES_TYPE, "Restaurants");
        startActivity(i);
    }

    private void setFocus(String placestype){
        if(placestype.equals("activity")){
            Activities.requestFocus();
        }else if(placestype.equals("pubs")){
            Pubs.requestFocus();
        }else if(placestype.equals("restaurant")){
            Restaurants.requestFocus();
        }
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
