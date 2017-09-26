package com.dhh.rxlifecycle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import rx.Observable;
import rx.subjects.BehaviorSubject;

/**
 * Created by dhh on 2017/9/25.
 */

public class LifecycleV4Fragment extends android.support.v4.app.Fragment implements LifecycleManager {
    private final BehaviorSubject<ActivityEvent> lifecycleSubject;

    public LifecycleV4Fragment() {
        lifecycleSubject = BehaviorSubject.create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        lifecycleSubject.onNext(ActivityEvent.onCreate);
        Log.d("LifecycleV4Fragment", "onCreate:");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        lifecycleSubject.onNext(ActivityEvent.onStart);
        Log.d("LifecycleV4Fragment", "onStart:");

        super.onStart();
    }

    @Override
    public void onResume() {
        lifecycleSubject.onNext(ActivityEvent.onResume);
        Log.d("LifecycleV4Fragment", "onResume:");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d("LifecycleV4Fragment", "onPause:");
        lifecycleSubject.onNext(ActivityEvent.onPause);
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d("LifecycleV4Fragment", "onStop:");
        lifecycleSubject.onNext(ActivityEvent.onStop);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d("LifecycleV4Fragment", "onDestroy:");
        lifecycleSubject.onNext(ActivityEvent.onDestory);
        super.onDestroy();
    }
    @Override
    public Observable<ActivityEvent> getLifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Override
    public <T> LifecycleTransformer<T> bindUntilEvent(final ActivityEvent activityEvent) {
        return new LifecycleTransformer<>(lifecycleSubject, activityEvent);
    }

    @Override
    public <T> LifecycleTransformer<T> bindToLifecycle() {
        return new LifecycleTransformer<>(lifecycleSubject);
    }

    @Override
    public <T> LifecycleTransformer<T> bindOnDestroy() {
        return bindUntilEvent(ActivityEvent.onDestory);
    }
}
