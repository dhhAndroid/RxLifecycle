package com.dhh.demo;

import android.app.Application;

import com.dhh.rxlifecycle.RxLifecycle;

/**
 * Created by dhh on 2017/9/27.
 */

public class RxLifecycleAPP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RxLifecycle.injectRxLifecycle(this);
    }
}
