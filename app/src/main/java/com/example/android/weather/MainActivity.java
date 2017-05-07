package com.example.android.weather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClientOption;
import com.baidu.location.service.LocationService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity {

    private Button add;
    private Button getLocation;
    private TextView temperature;
    private TextView weather;
    private TextView aqi;
    private TextView humidity;
    private TextView degree;
    private TextView location;
    private Thread t;
    private ProgressDialog progressDialog;
    public static Handler handler;
    public static String position = "";
    private static boolean isFirst = true;

    LocationService locationService = null;
    BDLocationListener myListener = new MyLocationListener();
    SwipeRefreshLayout swipeRefreshLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        add = (Button) findViewById(R.id.buttonId);
        getLocation = (Button) findViewById(R.id.locationId);
        temperature = (TextView) findViewById(R.id.tempId);
        weather = (TextView) findViewById(R.id.weatherId);
        aqi = (TextView) findViewById(R.id.aqiId);
        humidity = (TextView) findViewById(R.id.humidityId);
        degree = (TextView) findViewById(R.id.degreeId);
        location = (TextView) findViewById(R.id.city);
        add.setOnClickListener(new ButtonListener());
        getLocation.setOnClickListener(new ButtonListener());
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("正在获取位置信息,请稍候...");
        handler = new Myhandler();
        locationService = new LocationService(getApplicationContext());
        locationService = new LocationService(getApplicationContext());
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.registerListener(myListener);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setSize(10);
        swipeRefreshLayout.setColorSchemeResources(R.color.blue);
        swipeRefreshLayout.setOnRefreshListener(new reFreshListener());
        GetDataThread gt = new GetDataThread();
        t = new Thread(gt, "Refresh");
        if(isFirst) {
            progressDialog.show();
            LocationThread lt = new LocationThread();
            Thread t = new Thread(lt,"LocationThread");
            t.start();
        } else{
            t.start();
        }
    }

    public class ButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.buttonId :
                    Intent intent = new Intent(MainActivity.this, City.class);
                    startActivity(intent);
                    break;
                case R.id.locationId :
                    Toast.makeText(MainActivity.this, "已获取位置信息", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public static String getURLConnection(String path) {
        String xml = "";
        try {
//            URL url = new URL(path);
//            URLConnection conn = url.openConnection();
//            conn.connect();
//            InputStream in = conn.getInputStream();
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(path);
            HttpResponse response = client.execute(get);
            int code = response.getStatusLine().getStatusCode();
            Log.d("http", "code");
            if (code == 200) {
                InputStream reader = response.getEntity().getContent();
                BufferedReader buffer = new BufferedReader(new InputStreamReader(reader));
                String list = buffer.readLine();
                while (list != null) {
                    xml += list;
                    list = buffer.readLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xml;
    }

    public class reFreshListener implements SwipeRefreshLayout.OnRefreshListener {

        @Override
        public void onRefresh() {
            GetDataThread gt = new GetDataThread();
            Thread t = new Thread(gt, "Refresh");
            t.start();
        }
    }

    public class LocationThread implements Runnable{

        @Override
        public void run() {
            Initiation();
            locationService.start();
        }
    }

    private class GetDataThread implements Runnable {                                                        //获取当天天气线程

        @Override
        public void run() {
            String todayXML = "http://api.k780.com:88/?app=weather.today&weaid=";                   //实时天气API前缀
            String aqiXMl = "http://api.k780.com:88/?app=weather.pm25&weaid=";                      //空气指数API前缀
//            String futureXML = "http://api.k780.com:88/?app=weather.future&weaid=";                 //未来天气API前缀
            String suffixXML = "&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
            System.out.println(position);
            String todayWeather = getURLConnection(todayXML + position + suffixXML);
            System.out.println("todayWeather = " + todayWeather);
            String todayAqi = getURLConnection(aqiXMl + position + suffixXML);
            System.out.println("todayAqi = " + todayAqi);
            Message msg = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("todayWeather", todayWeather);
            bundle.putString("todayAqi", todayAqi);
            msg.setData(bundle);
            handler.sendMessage(msg);
        }
    }

    public class Myhandler extends Handler {                                                         //接受数据更改UI

        public void handleMessage(Message msg) {
            swipeRefreshLayout.setRefreshing(false);
            if(progressDialog.isShowing()){
                progressDialog.dismiss();
            }
            String todayWeather = msg.getData().getString("todayWeather");
            if (todayWeather != null) {
                try {
                    todayWeather = String.valueOf(new JSONObject(todayWeather).getJSONObject("result"));
                    JSONObject json = new JSONObject(todayWeather);
                    location.setText(json.getString("citynm"));
                    String temp = json.getString("temp_curr");
                    temperature.setText(temp);
                    degree.setText("o");
                    weather.setText(json.getString("weather"));
                    humidity.setText("湿度" + json.getString("humidity") + " " + json.getString("wind") + json.getString("winp"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String todayAqi = msg.getData().getString("todayAqi");
            if (todayAqi != null) {
                try {
                    todayAqi = String.valueOf(new JSONObject(todayAqi).getJSONObject("result"));
                    JSONObject json = new JSONObject(todayAqi);
                    aqi.setText("空气指数" + json.getString("aqi") + " " + json.getString("aqi_levnm") + "  " + json.getString("aqi_remark"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void Initiation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);                   //设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");                                                               //设置返回的定位结果坐标系
        option.setScanSpan(0);                                                                      //设置发起定位请求的间隔(需要大于等于1000ms才是有效的)
        option.setOpenGps(false);                                                                   //设置是否打开GPS
        option.setLocationNotify(true);                                                             //设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);                                                     //设置是否需要位置语义化结果
        option.setIsNeedLocationPoiList(true);                                                      //设置是否需要POI结果
        option.setIgnoreKillProcess(true);                                                          //设置是否在stop的时候杀死这个进程
        option.SetIgnoreCacheException(true);                                                       //设置是否收集CRASH信息
        option.setEnableSimulateGps(false);                                                         //设置是否需要过滤gps仿真结果
        locationService.setLocationOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {                              //GPS定位

            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {                    //网络定位
                String district = bdLocation.getDistrict();
                if(district != null) {
                    if(district.length() == 2) {
                        position = district;
                        System.out.println(position);
                    } else{
                        position = district.substring(0, district.length() - 1);
                    }
                    isFirst = false;
                    t.start();
                }
            } else if (bdLocation.getLocType() == BDLocation.TypeServerError) {                        //服务端网络定位失败

            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkException) {                   //网络不同定位失败

            } else if (bdLocation.getLocType() == BDLocation.TypeCriteriaException) {                  //无法获取有效定位依据

            }
        }
    }
}
