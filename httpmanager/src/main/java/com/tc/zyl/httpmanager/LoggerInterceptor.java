package com.tc.zyl.httpmanager;

import android.util.Log;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

final class LoggerInterceptor implements Interceptor {
    private static final String TAG = "AAA";
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private static final String LINE = "----------------------------------------------------------------";
    private static final String SEPARATOR = "****************************************************************";
    private static final int MAXLENGTH = 2000;

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Log.e(TAG, LINE);
        RequestBody requestBody = request.body();
        if (requestBody != null) {
            Log.e(TAG, String.format("%s %s%nContent-Type: %s%n%s", request.method(), request.url(), "" + requestBody.contentType(), request.headers().toString()));
            okio.Buffer buffer = new okio.Buffer();
            requestBody.writeTo(buffer);
            Log.e(TAG, "--> " + buffer.readString(UTF8));
        } else
            Log.e(TAG, String.format("%s %s%n%s", request.method(), request.url(), request.headers()));

        Log.e(TAG, LINE);

        Log.e(TAG, SEPARATOR);
        long start = System.nanoTime();
        Response response = chain.proceed(request);
        Log.e(TAG, String.format("Response for %s use %.1fms%n%s", response.request().url(), (System.nanoTime() - start) / 1e6d, response.headers().toString()));
        ResponseBody responseBody = response.body();
        String content = responseBody.string();
        print(content);
        Log.e(TAG, SEPARATOR);
        return response.newBuilder().body(ResponseBody.create(responseBody.contentType(), content)).build();
    }

    private void print(String s) {
        try {
            String msg = null;
            char c = s.charAt(0);
            if (c == '{')
                msg = new org.json.JSONObject(s).toString(2);
            else if (c == '[')
                msg = new org.json.JSONArray(s).toString(2);

            if (msg == null)
                Log.e(TAG, s);
            else {
                int length = msg.length();
                if (length < MAXLENGTH)
                    Log.e(TAG, msg);
                else {
                    int count = length / MAXLENGTH;
                    int left = length % MAXLENGTH;
                    for (int i = 0; i < count; i++) {
                        int start = i * MAXLENGTH;
                        Log.e(TAG, msg.substring(start, start + MAXLENGTH));
                    }
                    if (left > 0)
                        Log.e(TAG, msg.substring(count * MAXLENGTH));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, s);
        }
    }

}