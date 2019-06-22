package com.mtjinse.myapplication.activity.models;

import android.os.AsyncTask;
import android.util.Log;
import android.view.textclassifier.TextLinks;

import com.mtjinse.myapplication.R;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SendNotification {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void sendNotification(String regToken, String title, String messsage){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... parms) {
                try {
                    OkHttpClient client = new OkHttpClient();
                    JSONObject json = new JSONObject();
                    JSONObject dataJson = new JSONObject();
                    dataJson.put("smallIcon", R.drawable.ic_appicon);
                    dataJson.put("body", messsage);
                    dataJson.put("title", title);
                    json.put("notification", dataJson);
                    json.put("to", regToken);
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .header("Authorization", "key=" + "AAAAvCRbpiE:APA91bHEhJm8yff_fYBv8LSPd7cdwUi2_mm-VabiA1ol2ZJ_kb8cDs4VomRlZJ1N0LJWQJrxa8NA25BpSQfeHcSFQUZLWXjb54e2loelyzc9KKcPaTYPd6kMwkbAKXY5Wc7gWvZf5tEj")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                }catch (Exception e){
                    Log.d("error", e+"");
                }
                return  null;
            }
        }.execute();
    }
}
