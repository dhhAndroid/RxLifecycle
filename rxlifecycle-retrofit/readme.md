## RxLifecycle-Retrofit
[![](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html) 
[ ![Download](https://api.bintray.com/packages/dhhandroid/maven/rxlifecycle-retrofit/images/download.svg) ](https://bintray.com/dhhandroid/maven/rxlifecycle-retrofit/_latestVersion)
[ ![API](https://img.shields.io/badge/API-11%2B-blue.svg?style=flat-square) ](https://developer.android.com/about/versions/android-3.0.html)
[ ![License](http://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square) ](http://www.apache.org/licenses/LICENSE-2.0)
### 对于RxJava+Retrofit的网络请求框架的优化版本
## Gradle
```

	compile 'com.dhh:rxlifecycle-retrofit:1.3'

```
### 首先说明这个方式的实现原理是:通过自定义CallAdapterFactory,将Observable与activity绑定.每一个Activity绑定一个retrofit客户端,在每次创建service接口的时候,都要重新初始化Retrofit客户端,禁止复用.
## How to use

### 一. 如果你自己没有自定义RxJavaCallAdapterFactory,直接忽略这条操作.若你已经自定义了一个XXXRxJavaCallAdapterFactory(不是Retrofit自带的那个),又想使用本库提供的RxJavaLifecycleCallAdapterFactory,那么你需要这么做:
### 1. 在项目的清单文件(AndroidManifest.xml)application节点下加入meta-data.其中 name字段是你自己自定义的 XXXRxJavaCallAdapterFactory 的全路径,并且需要将这个Factory的构造方法改成public,不能是private.  value字段必须是 CallAdapterFactory .如下示例(类似Glide自定义模块):
```

	        <meta-data
	            android:name="com.dhh.progressmanager.ProgressRxJavaCallAdapterFactory"
	            android:value="CallAdapterFactory"/>


```
### 2. 在你项目的Application的onCreate方法中进行初始化:
```

		RxLifecycleRetrofit.init(this);

```
## ★★★ 如果在你的项目里没有自定义 XXXRxJavaCallAdapterFactory,请忽略第一步的配置.
### 二. 在使用retrofit将接口实例化的时候,切记一定要一同初始化一个新的Retrofit客户端,在addCallAdapterFactory的时候用项目里提供的RxJavaLifecycleCallAdapterFactory.createWithLifecycleManager(lifecycleManager),你可以在自己的项目里封装一个HttpHelper类,单例模式,将okhttpClient,以及baseUrl等等相关配置的东西都预先处理好,demo里的HttpHelper类如下:
```

		public class HttpHelper {
		    private static HttpHelper instance;
		    public String baseUrl = "http://api.nohttp.net/";
		    private OkHttpClient client;
		
		    public HttpHelper() {
		        client = new OkHttpClient.Builder()
		                //other config ...
		
		                .build();
		    }
		
		    public static HttpHelper getInstance() {
		        if (instance == null) {
		            synchronized (HttpHelper.class) {
		                if (instance == null) {
		                    instance = new HttpHelper();
		                }
		            }
		        }
		        return instance;
		    }
		
		    /**
		     * 不带生命周期自动绑定的
		     * @param service
		     * @param baseUrl
		     * @param <T>
		     * @return
		     */
		    public <T> T create(Class<T> service, String baseUrl) {
		        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
		                .addCallAdapterFactory(RxJavaLifecycleCallAdapterFactory.create())
		                .addConverterFactory(GsonConverterFactory.create())
		                .client(client)
		                .build();
		        return retrofit.create(service);
		    }
		
		    /**
		     * 不带生命周期自动绑定的
		     * @param service
		     * @param <T>
		     * @return
		     */
		    public <T> T create(Class<T> service) {
		        return create(service, baseUrl);
		    }
		
		    /**
		     * 带生命周期自动绑定的
		     * @param service
		     * @param baseUrl
		     * @param lifecycleManager
		     * @param <T>
		     * @return
		     */
		    public <T> T createWithLifecycleManager(Class<T> service, String baseUrl, LifecycleManager lifecycleManager) {
		        Retrofit retrofit = new Retrofit.Builder().baseUrl(baseUrl)
		                .addCallAdapterFactory(RxJavaLifecycleCallAdapterFactory.createWithLifecycleManager(lifecycleManager))
		                .addConverterFactory(GsonConverterFactory.create())
		                .client(client)
		                .build();
		        return retrofit.create(service);
		    }
		
		    /**
		     * 带生命周期自动绑定的
		     * @param service
		     * @param lifecycleManager
		     * @param <T>
		     * @return
		     */
		    public <T> T createWithLifecycleManager(Class<T> service, LifecycleManager lifecycleManager) {
		        return createWithLifecycleManager(service, baseUrl, lifecycleManager);
		    }
		
		    public void setBaseUrl(String baseUrl) {
		        this.baseUrl = baseUrl;
		    }
		}

```
### 其中 createWithLifecycleManager方法是核心方法,将LifecycleManager传入.
## 三. 实例化接口
### 在调用 createWithLifecycleManager 方法的时候,只需要在调用方法的地方能获取到对应Activity的LifecycleManager即可,比如在Activity中实例化接口(准确地说是动态代理出来的):
```

        LifecycleManager lifecycleManager = RxLifecycle.with(this);
        Api api = HttpHelper.getInstance().createWithLifecycleManager(Api.class, lifecycleManager);
        api.get("sds")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody body) {

                    }
                });
```
### 这样的话,这个get请求如果在Activity销毁的时候(onDestroy)还没有结束,就会直接结束掉.通过这个api接口发出的网络请求都会在Activity在onDestroy的时候取消订阅,防止内存泄漏.其次,我再内部直接将线程指定在RxJava的io线程,外部不用在重复写 subscribeOn(Schedulers.io()) 这行代码.至于 observeOn(AndroidSchedulers.mainThread()) 这行代码我没有在内部封装是因为用户可能要对数据做一些转化处理,也有可能比较耗时,所以切换主线程,在数据转换做完后比较好.
# 从此就可以对 使用RxJava+Retrofit 导致的内存泄漏说 ( ^_^ )/~~拜拜 !
## License
```

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```