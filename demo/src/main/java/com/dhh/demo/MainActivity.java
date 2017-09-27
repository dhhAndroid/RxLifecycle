package com.dhh.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dhh.rxlifecycle.ActivityEvent;
import com.dhh.rxlifecycle.LifecycleManager;
import com.dhh.rxlifecycle.LifecycleTransformer;
import com.dhh.rxlifecycle.RxLifecycle;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Action0;

public class MainActivity extends AppCompatActivity {

    private LifecycleManager mLifecycleManager;
    private MyTextView myTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mLifecycleManager = RxLifecycle.with(this);
        Observable.just(1)
                .compose(bindToLifecycle())
                .subscribe();
        Observable.just("34")
                .compose(this.<String>bindToLifecycle())
                .subscribe();

    }

    private <T> LifecycleTransformer<T> bindToLifecycle() {
        return RxLifecycle.with(this).bindToLifecycle();
    }

    private <T> LifecycleTransformer<T> bindOnDestroy() {
        return RxLifecycle.with(this).bindOnDestroy();
    }

    private <T> LifecycleTransformer<T> bindUntilEvent(ActivityEvent event) {
        return RxLifecycle.with(this).bindUntilEvent(event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Observable.just(1)
                .compose(bindToLifecycle())
                .subscribe();
        myTextView.RxLifeCycleSetText("dhhAndroid");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Observable.just(1)
                .compose(RxLifecycle.with(this).<Integer>bindToLifecycle())
                .subscribe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Observable.just("dhhAndroid")
                .compose(RxLifecycle.with(this).<String>bindOnDestroy())
                .subscribe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Observable.just("dhhAndroid")
                .compose(RxLifecycle.with(this).<String>bindOnDestroy())
                .subscribe();
        Observable.timer(10, TimeUnit.SECONDS)
                .compose(mLifecycleManager.<Long>bindToLifecycle())
                .subscribe();
        test();

    }

    private void test() {
        Observable.timer(10, TimeUnit.SECONDS)
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d("MainActivity", "注册");
                    }
                })
                .doOnUnsubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.d("MainActivity", "注销");
                    }
                })
                .compose(RxLifecycle.with(this).<Long>bindOnDestroy())
                .subscribe();
        Observable.timer(10, TimeUnit.SECONDS)
                .compose(RxLifecycle.with(this).<Long>bindToLifecycle())
                .subscribe();
        Observable.timer(10, TimeUnit.SECONDS)
                //当activity onstop 注销
                .compose(RxLifecycle.with(this).<Long>bindUntilEvent(ActivityEvent.onStop))
                .subscribe();
    }

    private void initView() {
        myTextView = (MyTextView) findViewById(R.id.myTextView);
    }
}
