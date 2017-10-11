package com.dhh.demo;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Url;
import rx.Observable;

/**
 * Created by dhh on 2017/10/11.
 */

public interface Api {
    @GET
    Observable<ResponseBody> get(@Url String url);
}
