package com.tc.zyl.httpmanager;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.ArrayMap;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Http统一管理
 */
public final class HttpManager {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    //private static final MediaType FORM = MediaType.parse("application/x-www-form-urlencoded");
    //private static final MediaType TEXT = MediaType.parse("text/plain; charset=utf-8");
    public static final String CODE_SUCCESS = "200";
    private final OkHttpClient mOkHttpClient;
    private final Gson gson;
    private final Handler mHandler;

    private static class Holder {
        private static final HttpManager instance = new HttpManager();
    }

    private HttpManager() {
        mHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
        mOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor())
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .build();
                        return chain.proceed(request);
                    }
                })
                .connectTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(8L, TimeUnit.SECONDS)
                .writeTimeout(8L, TimeUnit.SECONDS)
                .build();
    }

    public static GetBuilder get() {
        return new GetBuilder();
    }

    public static PostBuilder post() {
        return new PostBuilder();
    }

    public static PostJsonBuilder postJson() {
        return new PostJsonBuilder();
    }

    public static PostFileBuilder postFile() {
        return new PostFileBuilder();
    }

    public static class GetBuilder {

        private ArrayMap<String, String> map;
        private String url;
        private Request request;
        //private Object tag;

        private GetBuilder() {
            map = new ArrayMap<>();
        }

        public GetBuilder url(String url) {
            this.url = url;
            return this;
        }

        public GetBuilder addParams(String name, String value) {
            map.put(name, value);
            return this;
        }


        public GetBuilder build() {
            for (int i = 0; i < map.size(); i++) {
                if (i == 0)
                    url = url + "?" + map.keyAt(i) + "=" + map.valueAt(i);
                else
                    url = url + "&" + map.keyAt(i) + "=" + map.valueAt(i);

            }
            request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            return this;
        }

        public <T> void execute(ResponseCallback<T> callback) {
            Holder.instance.exec(request, callback);
        }

    }

    public static class PostBuilder {

        private ArrayMap<String, String> map;
        private String url;
        private Request request;
        //private Object tag;

        private PostBuilder() {
            map = new ArrayMap<>();
        }

        public PostBuilder url(String url) {
            this.url = url;
            return this;
        }

        public PostBuilder addParams(String name, String value) {
            map.put(name, value);
            return this;
        }


        public PostBuilder build() {
            FormBody.Builder builder = new FormBody.Builder();

            for (int i = 0; i < map.size(); i++)
                builder.add(map.keyAt(i), map.valueAt(i));

            request = new Request.Builder()
                    .url(url)
                    .post(builder.build())
                    //.tag(tag)
                    .build();

            return this;
        }

        public <T> void execute(ResponseCallback<T> callback) {
            Holder.instance.exec(request, callback);
        }

    }

    public static class PostJsonBuilder {

        private String url;
        private Request request;
        private String json;

        private PostJsonBuilder() {
        }

        public PostJsonBuilder url(String url) {
            this.url = url;
            return this;
        }

        public PostJsonBuilder addJson(String json) {
            this.json = json;
            return this;
        }


        public PostJsonBuilder build() {
            RequestBody requestBody = RequestBody.create(JSON, json);
            request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            return this;
        }

        public <T> void execute(ResponseCallback<T> callback) {
            Holder.instance.exec(request, callback);
        }

    }

    public static class PostFileBuilder {

        private ArrayMap<String, String> map;
        private String url;
        private String name;
        private File file;
        private Request request;
        //private Object tag;

        private PostFileBuilder() {
            map = new ArrayMap<>();
        }

        public PostFileBuilder url(String url) {
            this.url = url;
            return this;
        }

        public PostFileBuilder addParams(String name, String value) {
            map.put(name, value);
            return this;
        }

        public PostFileBuilder addFile(String name, File file) {
            this.name = name;
            this.file = file;
            return this;
        }


        public PostFileBuilder build() {
            MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
            for (int i = 0; i < map.size(); i++)
                builder.addFormDataPart(map.keyAt(i), map.valueAt(i));
            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
            builder.addFormDataPart(name, file.getName(), body);
            request = new Request.Builder()
                    .url(url)
                    .post(builder.build())
                    .build();
            return this;
        }

        public <T> void execute(ResponseCallback<T> callback) {
            Holder.instance.exec(request, callback);
        }

    }

    private <T> void exec(final Request request, final ResponseCallback<T> callback) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            final InnerRunnable runnable = new InnerRunnable(callback);

            @Override
            public void onFailure(Call call, IOException e) {
                mHandler.post(runnable);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        runnable.setTag(gson.fromJson(response.body().string(), ((ParameterizedType) callback.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0]));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mHandler.post(runnable);
            }
        });
    }

    private static class InnerRunnable<T> implements Runnable {

        private ResponseCallback<T> mCallback;
        private Object o;

        InnerRunnable(ResponseCallback<T> callback) {
            mCallback = callback;
        }

        void setTag(Object o) {
            this.o = o;
        }

        @Override
        public void run() {
            if (mCallback != null)
                if (o != null)
                    mCallback.onSuccess((T) o);
                else
                    mCallback.onError();
            else
                mCallback.onError();
        }

    }

    public interface ResponseCallback<T> {

        void onError();

        void onSuccess(T t);

    }

    /**
     * MD5加密
     */
    public static String md5(String str) {
        MessageDigest md;
        StringBuffer sb = new StringBuffer();
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            byte[] data = md.digest();
            int index;
            for (byte b : data) {
                index = b;
                if (index < 0) index += 256;
                if (index < 16) sb.append("0");
                sb.append(Integer.toHexString(index));
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}
