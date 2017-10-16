package com.dhh.rxlifecycle2;


import io.reactivex.Observable;

/**
 * Created by dhh on 2017/9/25.
 */

public interface LifecycleManager {

    Observable<ActivityEvent> getLifecycle();

    <T> LifecycleTransformer<T> bindUntilEvent(ActivityEvent activityEvent);

    <T> LifecycleTransformer<T> bindToLifecycle();

    <T> LifecycleTransformer<T> bindOnDestroy();
}
