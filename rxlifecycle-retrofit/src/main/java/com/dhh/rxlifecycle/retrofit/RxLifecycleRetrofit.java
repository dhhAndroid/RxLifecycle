package com.dhh.rxlifecycle.retrofit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import retrofit2.CallAdapter;

/**
 * Created by dhh on 2017/10/10.
 */

public class RxLifecycleRetrofit {
    private static final String RXJAVA_CALL_ADAPTER = "CallAdapterFactory";
    private static CallAdapter.Factory factory;

    public static void init(Context context) {
        try {
            Class.forName("retrofit2.Retrofit");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency retrofit2 !");
        }
        try {
            Class.forName("retrofit2.adapter.rxjava.RxJavaCallAdapterFactory");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency RxJavaCallAdapterFactory !");
        }
        Context applicationContext = context.getApplicationContext();
        try {
            ApplicationInfo appInfo = applicationContext.getPackageManager().getApplicationInfo(
                    applicationContext.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                for (String key : appInfo.metaData.keySet()) {
                    if (RXJAVA_CALL_ADAPTER.equals(appInfo.metaData.get(key))) {
                        factory = parseCallAdapterFactory(key);
                        return;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static CallAdapter.Factory getFactory() {
        return factory;
    }

    private static CallAdapter.Factory parseCallAdapterFactory(String className) {
        Class<?> clazz;
        try {
            clazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to find CallAdapter.Factory implementation", e);
        }

        Object module;
        try {
            module = clazz.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate CallAdapter.Factory implementation for " + clazz, e);
        }

        if (!(module instanceof CallAdapter.Factory)) {
            throw new RuntimeException("Expected instanceof CallAdapter.Factory, but found: " + module);
        }
        return (CallAdapter.Factory) module;
    }
}
