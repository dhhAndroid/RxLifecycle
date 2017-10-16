package com.dhh.rxlifecycle2;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Maybe;
import io.reactivex.MaybeSource;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static com.dhh.rxlifecycle2.ActivityEvent.onDestory;
import static com.dhh.rxlifecycle2.ActivityEvent.onPause;
import static com.dhh.rxlifecycle2.ActivityEvent.onStop;


/**
 * Created by dhh on 2017/9/25.
 */

public class LifecycleTransformer<T> implements ObservableTransformer<T, T>, FlowableTransformer<T, T>, SingleTransformer<T, T>, MaybeTransformer<T, T>, CompletableTransformer {
    private Observable<?> observable;


    LifecycleTransformer(Observable<ActivityEvent> lifecycleObservable) {
        Observable<ActivityEvent> observable = lifecycleObservable.share();
        this.observable = Observable.combineLatest(observable.take(1).map(ACTIVITY_LIFECYCLE), observable.skip(1),
                new BiFunction<ActivityEvent, ActivityEvent, Boolean>() {
                    @Override
                    public Boolean apply(@NonNull ActivityEvent activityEvent, @NonNull ActivityEvent activityEvent2) throws Exception {
                        return activityEvent.equals(activityEvent2);
                    }
                })
                .filter(new Predicate<Boolean>() {
                    @Override
                    public boolean test(@NonNull Boolean aBoolean) throws Exception {
                        return aBoolean;
                    }
                });

    }

    LifecycleTransformer(Observable<ActivityEvent> lifecycleObservable, final ActivityEvent activityEvent) {
        this.observable = lifecycleObservable
                .filter(new Predicate<ActivityEvent>() {
                    @Override
                    public boolean test(@NonNull ActivityEvent event) throws Exception {
                        return event.equals(activityEvent);
                    }
                })
                .take(1);
    }

    @Override
    public ObservableSource<T> apply(Observable<T> upstream) {
        return upstream.takeUntil(observable);
    }

    @Override
    public Publisher<T> apply(Flowable<T> upstream) {
        return upstream.takeUntil(observable.toFlowable(BackpressureStrategy.LATEST));
    }

    @Override
    public SingleSource<T> apply(Single<T> upstream) {
        return upstream.takeUntil(observable.firstOrError());
    }

    @Override
    public MaybeSource<T> apply(Maybe<T> upstream) {
        return upstream.takeUntil(observable.firstElement());
    }

    @Override
    public CompletableSource apply(Completable upstream) {
        return Completable.ambArray(upstream);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LifecycleTransformer<?> that = (LifecycleTransformer<?>) o;

        return observable.equals(that.observable);
    }

    @Override
    public int hashCode() {
        return observable.hashCode();
    }

    @Override
    public String toString() {
        return "LifecycleTransformer{" +
                "observable=" + observable +
                '}';
    }


    // Figures out which corresponding next lifecycle event in which to unsubscribe, for Activities
    private static final Function<ActivityEvent, ActivityEvent> ACTIVITY_LIFECYCLE =
            new Function<ActivityEvent, ActivityEvent>() {
                @Override
                public ActivityEvent apply(@io.reactivex.annotations.NonNull ActivityEvent lastEvent) throws Exception {
                    switch (lastEvent) {
                        case onCreate:
                            return onDestory;
                        case onStart:
                            return onStop;
                        case onResume:
                            return onPause;
                        case onPause:
                            return onStop;
                        case onStop:
                            return onDestory;
                        case onDestory:
                            throw new IllegalStateException("Cannot injectRxLifecycle to Activity lifecycle when outside of it.");
                        default:
                            throw new UnsupportedOperationException("Binding to " + lastEvent + " not yet implemented");
                    }
                }
            };
}
