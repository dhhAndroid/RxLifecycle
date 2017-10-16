package com.dhh.rxlifecycle2.retrofit;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhh.rxlifecycle2.LifecycleManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.CallAdapter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by dhh on 2017/10/10.
 */

public class RxJava2LifecycleCallAdapterFactory extends CallAdapter.Factory {

    private static CallAdapter.Factory adapterFactory;
    private LifecycleManager lifecycleManager;

    private RxJava2LifecycleCallAdapterFactory(LifecycleManager lifecycleManager) {
        this.lifecycleManager = lifecycleManager;
        try {
            Class.forName("retrofit2.Retrofit");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency retrofit2 !");
        }
        if (adapterFactory == null) {
            try {
                Class.forName("retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Must be dependency RxJava2CallAdapterFactory !");
            }
            adapterFactory = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());
        }
    }

    public static CallAdapter.Factory create() {
        try {
            Class.forName("retrofit2.Retrofit");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Must be dependency retrofit2 !");
        }
        if (adapterFactory == null) {
            try {
                Class.forName("retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Must be dependency RxJava2CallAdapterFactory !");
            }
            adapterFactory = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());
        }
        return adapterFactory;
    }

    /**
     * 确保你注入的factory内部是RxJava2CallAdapterFactory
     *
     * @param factory
     */
    public static void injectCallAdapterFactory(CallAdapter.Factory factory) {
        RxJava2LifecycleCallAdapterFactory.adapterFactory = factory;
    }

    public static CallAdapter.Factory createWithLifecycleManager(LifecycleManager lifecycleManager) {
        if (lifecycleManager == null) throw new NullPointerException("lifecycleManager == null");
        return new RxJava2LifecycleCallAdapterFactory(lifecycleManager);
    }

    @Nullable
    @Override
    public CallAdapter<?, ?> get(@NonNull Type returnType, @NonNull Annotation[] annotations, @NonNull Retrofit retrofit) {
        return new RxJavaLifecycleCallAdapter<>(adapterFactory.get(returnType, annotations, retrofit), lifecycleManager);
    }

    private static final class RxJavaLifecycleCallAdapter<R> implements CallAdapter<R, Observable<?>> {
        private CallAdapter<R, ?> callAdapter;
        private LifecycleManager lifecycleManager;

        public RxJavaLifecycleCallAdapter(CallAdapter<R, ?> callAdapter, LifecycleManager lifecycleManager) {
            this.callAdapter = callAdapter;
            this.lifecycleManager = lifecycleManager;
        }

        @Override
        public Type responseType() {
            return callAdapter.responseType();
        }

        @Override
        public Observable<?> adapt(@NonNull Call<R> call) {
            Observable<?> observable = (Observable) callAdapter.adapt(call);
            return observable
                    .compose(lifecycleManager.bindOnDestroy())
                    .subscribeOn(Schedulers.io());
        }

    }
}
