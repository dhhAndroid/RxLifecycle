package com.dhh.demo;

import android.app.Application;

import com.dhh.rxlifecycle.RxLifecycle;
import com.dhh.rxlifecycle.retrofit.RxJavaLifecycleCallAdapterFactory;
import com.dhh.rxlifecycle2.retrofit.RxJava2LifecycleCallAdapterFactory;

import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import rx.schedulers.Schedulers;

/**
 * Created by dhh on 2017/9/27.
 */

public class RxLifecycleAPP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RxLifecycle.injectRxLifecycle(this);
        com.dhh.rxlifecycle2.RxLifecycle.injectRxLifecycle(this);

        //如果你有一个自定义的XXXRxJavaCallAdapterFactory,这样注入
        // 假如这是你自己定义的XXXRxJavaCallAdapterFactory
        RxJavaCallAdapterFactory yourFactory = RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io());
        //RxJava1 CallAdpater
        RxJavaLifecycleCallAdapterFactory.injectCallAdapterFactory(yourFactory);
        //RxJava2 CallAdpater
        RxJava2LifecycleCallAdapterFactory.injectCallAdapterFactory(RxJava2CallAdapterFactory.create());

    }
}
