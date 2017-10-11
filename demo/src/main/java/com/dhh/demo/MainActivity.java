package com.dhh.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dhh.rxlifecycle.ActivityEvent;
import com.dhh.rxlifecycle.LifecycleManager;
import com.dhh.rxlifecycle.LifecycleTransformer;
import com.dhh.rxlifecycle.RxLifecycle;
import com.dhh.websocket.RxWebSocketUtil;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    private LifecycleManager mLifecycleManager;
    private MyTextView myTextView;
    private Subscription mSubscription;

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

        mSubscription = Observable.just(1).subscribe();
        Observable.just(1, 23, 434, 5454, 343, 346, 56, 67, 4, -1)
                //取前五个就注销
                .take(5)
                //直到条件满足,注销
                .takeUntil(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer > 66666;
                    }
                })
                //直到另外一个Observable发送数据就注销,本库主要用的这个操作符
                .takeUntil(Observable.just(1))
                .first(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer integer) {
                        return integer == 111;
                    }
                })
                .map(new Func1<Integer, Integer>() {
                    @Override
                    public Integer call(Integer integer) {
                        if (integer < 0) {
                            //抛异常注销,这种用法在我另外一个库RxProgressManager使用到
                            throw new RuntimeException("数据不能小于0");
                        }
                        return integer;
                    }
                })
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (integer == 666) {
                            //当满足条件注销
                            unsubscribe();
                        }
                    }
                });
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
        RxWebSocketUtil.getInstance().setShowLog(BuildConfig.DEBUG);
        RxWebSocketUtil.getInstance().getWebSocketString("ws://127.0.0.1:8089")
                .compose(RxLifecycle.with(this).<String>bindToLifecycle())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {

                    }
                });
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
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
