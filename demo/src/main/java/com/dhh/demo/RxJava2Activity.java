package com.dhh.demo;

import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.dhh.rxlifecycle2.LifecycleManager;
import com.dhh.rxlifecycle2.RxLifecycle;
import com.dhh.rxlifecycle2.retrofit.HttpHelper;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.converter.gson.GsonConverterFactory;

public class RxJava2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rx_iava2);
        LifecycleManager lifecycleManager = RxLifecycle.with(this);
        Observable.just(2)
                .compose(lifecycleManager.<Integer>bindOnDestroy())
                .subscribe();
        //初始化HttpHelper
        HttpHelper.getInstance().setBaseUrl("https://github.com/dhhAndroid/");
        HttpHelper.getInstance().setClient(new OkHttpClient());
        HttpHelper.getInstance().setConverterFactory(GsonConverterFactory.create());
        final Api api = HttpHelper.getInstance().createWithLifecycleManager(Api.class, lifecycleManager);
        Observable.timer(3, TimeUnit.SECONDS)
                .flatMap(new Function<Long, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(@NonNull Long aLong) throws Exception {
                        return api.RxJava2get("https://github.com/dhhAndroid/RxLifecycle/blob/master/readme.md")
                                .map(new Function<ResponseBody, String>() {
                                    @Override
                                    public String apply(@NonNull ResponseBody body) throws Exception {
                                        return body.string();
                                    }
                                });
                    }
                })
                .doOnDispose(new Action() {
                    @Override
                    public void run() throws Exception {
                        Looper.prepare();
                        Toast.makeText(RxJava2Activity.this, "网络请求取消/完成了 !", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d("RxJava2Activity", s);
                        Toast.makeText(RxJava2Activity.this, s, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
