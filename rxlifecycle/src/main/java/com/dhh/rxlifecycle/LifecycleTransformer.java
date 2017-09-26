package com.dhh.rxlifecycle;

import android.support.annotation.NonNull;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.subjects.BehaviorSubject;

/**
 * Created by dhh on 2017/9/25.
 */

class LifecycleTransformer<T> implements Observable.Transformer<T, T> {
    private Observable<ActivityEvent> lifecycleObservable;
    private ActivityEvent activityEvent;

    LifecycleTransformer(BehaviorSubject<ActivityEvent> lifecycleObservable) {
        this.lifecycleObservable = lifecycleObservable.share();
    }

    LifecycleTransformer(BehaviorSubject<ActivityEvent> lifecycleObservable, ActivityEvent activityEvent) {
        this.lifecycleObservable = lifecycleObservable;
        this.activityEvent = activityEvent;
    }

    @Override
    public Observable<T> call(Observable<T> sourceObservable) {
        return sourceObservable.takeUntil(getLifecycleObservable());
    }

    @NonNull
    private Observable<?> getLifecycleObservable() {
        if (activityEvent != null) {
            lifecycleObservable.takeFirst(new Func1<ActivityEvent, Boolean>() {
                @Override
                public Boolean call(ActivityEvent event) {
                    return activityEvent == event;
                }
            });
        }
        return Observable.combineLatest(lifecycleObservable.first().map(ACTIVITY_LIFECYCLE),
                lifecycleObservable.skip(1), new Func2<ActivityEvent, ActivityEvent, Boolean>() {
                    @Override
                    public Boolean call(ActivityEvent activityEvent, ActivityEvent event) {
                        return activityEvent == event;
                    }
                })
                .takeFirst(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
                    }
                });
    }


    // Figures out which corresponding next lifecycle event in which to unsubscribe, for Activities
    private static final Func1<ActivityEvent, ActivityEvent> ACTIVITY_LIFECYCLE =
            new Func1<ActivityEvent, ActivityEvent>() {
                @Override
                public ActivityEvent call(ActivityEvent lastEvent) {
                    switch (lastEvent) {
                        case onCreate:
                            return ActivityEvent.onDestory;
                        case onStart:
                            return ActivityEvent.onStop;
                        case onResume:
                            return ActivityEvent.onPause;
                        case onPause:
                            return ActivityEvent.onStop;
                        case onStop:
                            return ActivityEvent.onDestory;
                        case onDestory:
                            throw new IllegalStateException("Cannot bind to Activity lifecycle when outside of it.");
                        default:
                            throw new UnsupportedOperationException("Binding to " + lastEvent + " not yet implemented");
                    }
                }
            };
}
