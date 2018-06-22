package com.direct2guests.d2g_tv.Activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.direct2guests.d2g_tv.NonActivity.NetworkConnection;
import com.direct2guests.d2g_tv.NonActivity.Variable;
import com.direct2guests.d2g_tv.NonActivity.VolleyCallback;
import com.direct2guests.d2g_tv.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;

import static com.direct2guests.d2g_tv.NonActivity.Constant.ImgPath;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ServerUrl;


public class LauncherActivity extends Activity {
    private NetworkConnection nc = new NetworkConnection();
    private Variable vdata;
    public final static String WATCHTV_FROM = "com.direct2guests.d2g_tv.WATCHTV_FROM";

    private FrameLayout watchtv_frame;
    private FrameLayout hotelservices_frame;
    private RelativeLayout weatherLayout;
    private TextView guestname_txtview;
    private TextView date_txtview;
    private TextView watctv_txt_launcher, weather_description, weather_temp;
    private TextView hotelservices_txt_launcher;
    private ImageView hotellogo_imgview, weather_icon;
    private TextClock textClock;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramThrowable) {
                Log.d("Error", "UncaughtException!!!");
                System.exit(2);
            }
        });
        Bundle configBundle = new Bundle();
        try {
            setContentView(R.layout.activity_launcher);
        }catch (RuntimeException e){
            onCreate(configBundle);
        }

        AnalyticsApplication application = (AnalyticsApplication) getApplicationContext();
        mTracker = application.getDefaultTracker();

        Date date = new Date();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
        String currentDateString = dateFormat.format(date);
        vdata = (Variable)getIntent().getSerializableExtra(Variable.EXTRA);

        //init widgets
        date_txtview = findViewById(R.id.dateTxt);
        watchtv_frame = findViewById(R.id.watchtvframe);
        hotelservices_frame = findViewById(R.id.hotelservicesframe);
        guestname_txtview = findViewById(R.id.welcomeGuest);
        hotellogo_imgview = findViewById(R.id.hotelLogo);
        watctv_txt_launcher = findViewById(R.id.watchtv_launcher_text);
        hotelservices_txt_launcher = findViewById(R.id.hotelservices_launcher_text);
        watctv_txt_launcher = findViewById(R.id.watchtv_launcher_text);
        hotelservices_txt_launcher = findViewById(R.id.hotelservices_launcher_text);
        weather_description = findViewById(R.id.LweatherDesc);
        weather_icon = findViewById(R.id.LweatherIcon);
        weather_temp = findViewById(R.id.LweatherTemp);
        weatherLayout = findViewById(R.id.weatherL);
        // Font path
        String fontPathReg = "raleway/Raleway-Regular.ttf";
        String fontPathRegBold = "raleway/Raleway_Bold.ttf";
        String fontPathBold = "fonts/CaviarDreams_Bold.ttf";

        // text view label
        date_txtview = findViewById(R.id.dateTxt);
        textClock = findViewById(R.id.textClock);

        // Loading Font Face
        Typeface fontReg = Typeface.createFromAsset(getAssets(), fontPathReg);
        Typeface fontRegBold = Typeface.createFromAsset(getAssets(), fontPathRegBold);
        Typeface fontBold = Typeface.createFromAsset(getAssets(), fontPathBold);

        //Applying font
        watctv_txt_launcher.setTypeface(fontReg);
        hotelservices_txt_launcher.setTypeface(fontReg);
        date_txtview.setTypeface(fontBold);
        textClock.setTypeface(fontBold);
        guestname_txtview.setTypeface(fontRegBold);

        date_txtview.setText(currentDateString);
        guestname_txtview.setText("Welcome "+vdata.getGuestFirstName()+" !");

//        Picasso.with(getApplicationContext()).load(vdata.getServerURL()+vdata.getHotelLogo()).into(hotellogo_imgview);

        onFocusFrames();
    }

    @Override
    protected void onStart(){
        super.onStart();
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        getWeatherReport();

        mTracker.setScreenName(vdata.getHotelName()+" ~ Room No. "+vdata.getRoomNumber()+" ~ "+"Main View");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onResume(){
        super.onResume();
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
    }

    @Override
    public void onBackPressed(){
        //super.onBackPressed();
        /*Intent i = new Intent(this, MainActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        startActivity(i);*/
        super.onBackPressed();
    }
    public void watchtv_activity(View view){
        Intent i = new Intent(this, ChannelListActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        i.putExtra(WATCHTV_FROM, "launcher");
        startActivity(i);

    }

    public void hotelservices_activity(View view){
        Intent i = new Intent(this, HotelServicesActivity.class);
        i.putExtra(Variable.EXTRA, vdata);
        startActivity(i);
    }

    public void onFocusFrames(){
        watchtv_frame.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusAnim(view,hasFocus,getApplicationContext(),LauncherActivity.this);
            }
        });
        hotelservices_frame.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                vdata.focusAnim(view,hasFocus,getApplicationContext(),LauncherActivity.this);
            }
        });


    }

    private void getWeatherReport(){
        String url = "http://api.openweathermap.org/data/2.5/weather?id="+vdata.getWeatherid()+"&appid=f4d527523a8ec6989f6e543ffc16ea25&units=metric";
        nc.getdataObject(url, getApplicationContext(), new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if(response.getString("cod").toString().equals("200")) {
                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject weat = weather.getJSONObject(0);
                        String main = weat.getString("main");
                        String description = weat.getString("description");
                        String icon = weat.getString("icon");
                        JSONObject ma = response.getJSONObject("main");
                        String temp = ma.get("temp").toString();

                        String desc = main + "( " + description + " )";
                        String icon_url = "http://openweathermap.org/img/w/" + icon + ".png";
                        String tem = temp + "ᵒC";

                        weather_description.setText(desc);
                        weather_temp.setText(tem);
                        Picasso.with(getApplicationContext()).load(icon_url).resize(80, 80).into(weather_icon);
                        weatherLayout.setVisibility(View.VISIBLE);
                    }
                    else{
                        weatherLayout.setVisibility(View.GONE);
                    }
                }catch (JSONException e){
                    Log.d("Weather Error",e.getLocalizedMessage());
                }

            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }

}
