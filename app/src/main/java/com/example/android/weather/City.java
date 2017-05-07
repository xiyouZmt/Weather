package com.example.android.weather;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Android on 2015/12/17.
 */
public class City extends Activity{

    private Button back;
    private Button location;
    private Button go;
    private EditText editText;
    private String str;
    private MyHandler handler;
    public static Message msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.city);
        back = (Button) findViewById(R.id.backId);
        go = (Button) findViewById(R.id.goId);
        location = (Button) findViewById(R.id.locationId);
        editText = (EditText) findViewById(R.id.editTextId);
        handler = new MyHandler();
        back.setOnClickListener(new ButtonListener());
        go.setOnClickListener(new ButtonListener());
        location.setOnClickListener(new ButtonListener());
    }

    public class ButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.backId :                                                                  //返回上一个Activity
                    finish();
                case R.id.locationId :                                                              //定位

                case R.id.goId :
                    str = editText.getText().toString();
                    if(str.equals("")){}
                    else {
                        MainActivity.position = str;
                        thread t = new thread();
                        Thread T = new Thread(t, "Refresh");
                        T.start();
                        System.out.println("123456");
                    }
            }
        }
    }

    private class thread implements Runnable {                                                       //获取当天天气线程

        @Override
        public void run() {
            String todayXML = "http://api.k780.com:88/?app=weather.today&weaid=";                   //实时天气API前缀//未来天气API前缀
            String suffixXML = "&&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";
            try {
                String todayWeather = MainActivity.getURLConnection(todayXML + str + suffixXML);
                JSONObject json = new JSONObject(todayWeather);
                if(json.has("result")){
                    handler.sendEmptyMessage(1);
                } else{
                    handler.sendEmptyMessage(0);
                }
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    private class MyHandler extends Handler {

        public void handleMessage(Message msg){
            switch (msg.what) {
                case 0:
                    Toast.makeText(City.this, "无法找到该城市", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Intent intent = new Intent(City.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }
}
