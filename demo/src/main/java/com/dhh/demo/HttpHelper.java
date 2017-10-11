package com.dhh.demo;

import com.dhh.rxlifecycle.LifecycleManager;
import com.dhh.rxlifecycle.retrofit.RxJavaLifecycleCallAdapterFactory;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by dhh on 2017/10/11.
 */

public class HttpHelper {
    private static HttpHelper instance;
    public String baseUrl = "http://api.nohttp.net/";
    private OkHttpClient client;

    public HttpHelper() {
        client = new OkHttpClient.Builder()
                //other config ...

                .build();
    }

    public static HttpHelper getInstance() {
        if (instance == null) {
            synchronized (HttpHelper.class) {
                if (instance == null) {
                    instance = new HttpHelper();
                }
            }
        }
        return instance;
    }

    /**
     * 不带生命周期自动绑定的
     *
     * @param service
     * @param baseUrl
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> service, String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaLifecycleCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(service);
    }

    /**
     * 不带生命周期自动绑定的
     *
     * @param service
     * @param <T>
     * @return
     */
    public <T> T create(Class<T> service) {
        return create(service, baseUrl);
    }

    /**
     * 带生命周期自动绑定的
     *
     * @param service
     * @param baseUrl
     * @param lifecycleManager
     * @param <T>
     * @return
     */
    public <T> T createWithLifecycleManager(Class<T> service, String baseUrl, LifecycleManager lifecycleManager) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaLifecycleCallAdapterFactory.createWithLifecycleManager(lifecycleManager))
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit.create(service);
    }

    /**
     * 带生命周期自动绑定的
     *
     * @param service
     * @param lifecycleManager
     * @param <T>
     * @return
     */
    public <T> T createWithLifecycleManager(Class<T> service, LifecycleManager lifecycleManager) {
        return createWithLifecycleManager(service, baseUrl, lifecycleManager);
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
