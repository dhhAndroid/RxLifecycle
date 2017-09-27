package com.dhh.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhh.rxlifecycle.RxLifecycle;

import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by dhh on 2017/9/27.
 */

public class MyV4Fragment extends Fragment {
    private MyTextView myTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        myTextView = (MyTextView) view.findViewById(R.id.myTextView);
        Observable.timer(10, TimeUnit.SECONDS)
                .compose(RxLifecycle.with(this).<Long>bindToLifecycle())
                .subscribe();

    }

    @Override
    public void onStart() {
        super.onStart();
        myTextView.RxLifeCycleSetText("dhhAndroid");
    }
}
