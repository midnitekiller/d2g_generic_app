package com.direct2guests.d2g_tv.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.volley.VolleyError;
import com.direct2guests.d2g_tv.NonActivity.ChannelListAdapter;
import com.direct2guests.d2g_tv.NonActivity.NetworkConnection;
import com.direct2guests.d2g_tv.NonActivity.Variable;
import com.direct2guests.d2g_tv.NonActivity.VolleyCallback;
import com.direct2guests.d2g_tv.NonActivity.VolleyCallbackArray;
import com.direct2guests.d2g_tv.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.direct2guests.d2g_tv.NonActivity.Constant.ApiBasePath;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ApiUrl;
import static com.direct2guests.d2g_tv.NonActivity.Constant.ServerUrl;

public class ChannelListActivity extends Activity {
    private Variable vdata;
    private NetworkConnection nc = new NetworkConnection();
    private String[] channelTitle, channelURL;
    private ListView channel_listview;

    private Tracker mTracker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        setContentView(R.layout.activity_channel_list);

        AnalyticsApplication application = (AnalyticsApplication) getApplicationContext();
        mTracker = application.getDefaultTracker();
        vdata = (Variable)getIntent().getSerializableExtra(Variable.EXTRA);

        channel_listview = findViewById(R.id.channelListview);

    }

    @Override
    protected void onStart(){
        super.onStart();
        //start code hide status bar
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        //end code hide status bar
        mTracker.setScreenName(vdata.getHotelName()+" ~ Room No. "+vdata.getRoomNumber()+" ~ "+"Channel List View");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        getChannels();
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

    private void getChannels(){
        String url = vdata.getApiUrl() + "channels.php?hotel_id=" + vdata.getHotelID();
        nc.getdataArray(url, getApplicationContext(), new VolleyCallbackArray() {
            @Override
            public void onSuccess(JSONArray response) {
                    JSONArray results = response;
                    String serverss_url = vdata.getServerURL().toString();


                    ArrayList<JSONObject> channel_list = getArrayListFromJSONArray(results);
                    final ChannelListAdapter channel_adapter = new ChannelListAdapter(getApplicationContext(), R.layout.channel_item_list,channel_list,serverss_url);
                    channel_listview.setAdapter(channel_adapter);
                    channel_adapter.notifyDataSetChanged();
                    channel_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            channel_adapter.setPosition(i);
                            channel_adapter.notifyDataSetChanged();



                            Intent z = new Intent(ChannelListActivity.this, WatchTVActivity.class);
                            z.putExtra("CHANNEL_NUM", i);
                            z.putExtra(Variable.EXTRA, vdata);
                            z.putExtra(LauncherActivity.WATCHTV_FROM, "hotelservices");
                            startActivity(z);
                        }
                    });
                    channel_listview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            channel_adapter.setPosition(i);
                            channel_adapter.notifyDataSetChanged();

                        
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
            }

            @Override
            public void onError(VolleyError error) {

            }
        });
    }

    private ArrayList<JSONObject> getArrayListFromJSONArray(JSONArray jsonArray){

        ArrayList<JSONObject> aList=new ArrayList<JSONObject>();
        try {
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    aList.add(jsonArray.getJSONObject(i));
                }
            }
        }catch (JSONException je){je.printStackTrace();}
        return  aList;
    }

}
