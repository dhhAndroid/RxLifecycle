package com.dhh.rxlifecycle.retrofit;

import android.support.annotation.Nullable;

import com.dhh.rxlifecycle.LifecycleManager;

import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by dhh on 2017/10/11.
 */

public class HttpHelper {
    private static HttpHelper instance;
    public String baseUrl;
    private OkHttpClient client;
    private CallAdapter.Factory callAdapterFactory;
    private Converter.Factory converterFactory;

    private HttpHelper() {
        try {
            Class.forName("okhttp3.OkHttpClient");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency retrofit2 !");
        }
        client = new OkHttpClient.Builder()
                //other config ...

                .build();
        callAdapterFactory = RxJavaLifecycleCallAdapterFactory.create();
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(callAdapterFactory)
                .addConverterFactory(checkNotNull(converterFactory, "converterFactory == null"))
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addCallAdapterFactory(RxJavaLifecycleCallAdapterFactory.createWithLifecycleManager(lifecycleManager))
                .addConverterFactory(checkNotNull(converterFactory, "converterFactory == null"))
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
        this.baseUrl = checkNotNull(baseUrl, "baseUrl == null");
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void setClient(OkHttpClient client) {
        this.client = checkNotNull(client, "client == null");
    }

    public void setConverterFactory(Converter.Factory converterFactory) {
        this.converterFactory = checkNotNull(converterFactory, "converterFactory == null");
    }

    static <T> T checkNotNull(@Nullable T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}
