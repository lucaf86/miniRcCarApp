package com.example.minirccarapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements Application.ActivityLifecycleCallbacks {

    private static boolean background = false;
    private WebView mywebView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout no_internet_layout;
    private RelativeLayout mDns_discover_layout;
    private mDnsDiscover mDnsDiscover;

    //public String espIPAddr = "null";

    //public String espCarFound() { return espIPAddr; }

    @SuppressLint("HandlerLeak")
    final private Handler handler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case 1:
                    swipeRefreshLayout.setEnabled(false);
                    break;
                case 2:
                    swipeRefreshLayout.setEnabled(true);
                    break;
                case 3:
                    mDnsDiscover.discoverStop();
                    Log.e(TAG, "esp car found IP: " + mDnsDiscover.getIp());
                    loadWebPage(mDnsDiscover.getIp());
                    break;
            }
        }
    };

    private void loadWebPage(String ipAddr) {
        //mywebView.loadUrl("https://google.com");
        mywebView.loadUrl("http://" + ipAddr + "/index.htm");
        no_internet_layout.setVisibility(View.GONE);
        mDns_discover_layout.setVisibility(View.GONE);
        mywebView.setVisibility(View.VISIBLE);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("APP_ON_CREATE", "APP onCreate");
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Log.w("APP_ON_CREATE", "APP onCreate called the first time");
        }
        registerActivityLifecycleCallbacks(this);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_main);

        //    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        this.getWindow().getDecorView().setSystemUiVisibility(flags);

        // Code below is to handle presses of Volume up or Volume down.
        // Without this, after pressing volume buttons, the navigation bar will
        // show up and won't hide
        final View decorView = getWindow().getDecorView();
        decorView
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
                });

        Context mContext = getApplicationContext();

/*
        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();


        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);


        initializeResolveListener();
        initializeListener();


        mNsdManager.discoverServices(SERVICE_TYPE, mNsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

        //multicastLock.release(); // release after browsing
*/
        mywebView = (WebView) findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.webView_reload);
        no_internet_layout = findViewById(R.id.no_internet_layout);
        mDns_discover_layout = findViewById(R.id.mDns_discover_layout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mywebView.reload();
            }
        });

        mywebView.setWebChromeClient(new WebChromeClient());
        mywebView.setWebViewClient(new BrowserClient(swipeRefreshLayout));
        WebSettings webSettings = mywebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //mywebView.addJavascriptInterface(AndroidJSInterface, "Android");
        mywebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void webViewGamepadViewSet() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                handler.sendEmptyMessage(1);
                Log.d("webViewGamepadViewSet", "webViewGamepadViewSet() called from JS");
            }
        }, "Android");


        //mywebView.loadUrl("http://" + espIp + "/index.htm");
        mDnsDiscover = new mDnsDiscover(mContext);
        checkNetwor();
        //loadWebPage();
        //mywebView.evaluateJavascript("setWebView();", null);
        //Log.w(TAG, "setWebView() called from android");
    }

    class mDnsDiscover {
        //private Context mContext;
        final private NsdManager mNsdManager;
        private NsdManager.ResolveListener mResolveListener;
        private NsdManager.DiscoveryListener mDiscoveryListener;
        private WifiManager.MulticastLock mMulticastLock;

        final private String SERVICE_TYPE = "_espcar._tcp.";
        final private String SERVICE_NAME = "mini-rc-car";
        final private WifiManager mWifi;

        boolean isStared = false;

        private String ipAddr = "";

        public String getIp() {
            return this.ipAddr;
        }

        public mDnsDiscover(Context context) {
            //this.mContext = context;
            this.mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            this.mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            this.initializeResolveListener();
            this.initializeListener();
            this.isStared = false;
        }
        private boolean isConnectedToThisServer(String host) {
            Runtime runtime = Runtime.getRuntime();
            try {
                Process ipProcess = runtime.exec("/system/bin/ping -w 60 -c 1 " + host);
                int exitValue = ipProcess.waitFor();
                return (exitValue == 0);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return false;
        }


        private void discoverStop()
        {
            if(this.isStared) {
                this.mNsdManager.stopServiceDiscovery(this.mDiscoveryListener);
                this.mMulticastLock.release(); // release after browsing
                this.isStared = false;
            }
        }

        private void discoverStart() {
            this.mMulticastLock = mWifi.createMulticastLock("multicastLock");
            this.mMulticastLock.setReferenceCounted(true);
            this.mMulticastLock.acquire();

            this.mNsdManager.discoverServices(SERVICE_TYPE, this.mNsdManager.PROTOCOL_DNS_SD, this.mDiscoveryListener);
            this.isStared = true;
        }

        void initializeResolveListener() {
            Log.e(TAG, "initializeResolveListener ... ");
            this.mResolveListener = new NsdManager.ResolveListener() {
                @Override
                public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                    // Called when the resolve fails.  Use the error code to debug.
                    Log.e(TAG, "Resolve failed" + errorCode);
                }

                @Override
                public void onServiceResolved(NsdServiceInfo serviceInfo) {
                    Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

              /*  if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }*/
                    NsdServiceInfo service = serviceInfo;
                    //int port = service.getPort();
                    InetAddress host = service.getHost(); // getHost() will work now
                    Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                    String hostAddr;
                    if (Objects.requireNonNull(host.getHostAddress()).startsWith("/")) {
                        Log.d(TAG, "IP: " + host.getHostAddress().substring(1));
                        hostAddr = host.getHostAddress().substring(1);
                    } else {
                        hostAddr = host.getHostAddress();
                    }
                    Log.d(TAG, "host IP: " + hostAddr);

                    if (isConnectedToThisServer(hostAddr)) {
                        ipAddr = hostAddr;
                        handler.sendEmptyMessage(3);
                    }
                }
            };
            Log.e(TAG, "initializeResolveListener ... END");
        }


        // Instantiate a new DiscoveryListener
        //NsdManager.DiscoveryListener mDiscoveryListener;
        // private NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
        void initializeListener() {
            Log.e(TAG, "initializeListener ... ");
            this.mDiscoveryListener = new NsdManager.DiscoveryListener() {

                String TAG = "NSDFINDER";
                //NsdManager mNsdManager;
                //NsdManager.DiscoveryListener mDiscoveryListener;
                //String mServiceName;
                //String SERVICE_TYPE = "_http._tcp.";
                //String SERVICE_TYPE = "_espcar._tcp.";
                //String serviceName;
                //int mDiscoveryActive = 0;
                //NsdManager.ResolveListener mResolveListener;
                //NsdServiceInfo mService;
                //int mServiceport;
                //InetAddress mServicehostAdress;

                // Called as soon as service discovery begins.
                @Override
                public void onDiscoveryStarted(String regType) {
                    Log.d(TAG, "Service discovery started");
                }

                @Override
                public void onServiceFound(NsdServiceInfo service) {
                    // A service was found! Do something with it.

                    //mNsdManager.resolveService(service, mResolveListener);
                    //mNsdManager.stopServiceDiscovery(mDiscoveryListener)
                    Log.d(TAG, "Service discovery success :: " + service);

                    // host and port not yet availbale her, need to call resolveService() to decode them
                    if (service.getServiceType().equals(SERVICE_TYPE)) {
                        //if (service.getServiceName().equals(SERVICE_NAME))
                        if (service.getServiceName().contains(SERVICE_NAME)) {
                            mNsdManager.resolveService(service, mResolveListener);
                        }
                    }
                }

                @Override
                public void onServiceLost(NsdServiceInfo service) {
                    // When the network service is no longer available.
                    // Internal bookkeeping code goes here.
                    Log.e(TAG, "service lost: " + service);
                }

                @Override
                public void onDiscoveryStopped(String serviceType) {
                    Log.i(TAG, "Discovery stopped: " + serviceType);
                }

                @Override
                public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    //mNsdManager.stopServiceDiscovery(this);
                }

                @Override
                public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                    Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                    //mNsdManager.stopServiceDiscovery(this);
                }
            };
            Log.e(TAG, "initializeListener ... END");
        }
    }

    private void checkNetwor() {

        ConnectivityManager cm = (ConnectivityManager) MainActivity.this
                .getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo!=null && networkInfo.isConnectedOrConnecting()){
            mDnsDiscover.discoverStart();
        }else {
            no_internet_layout.setVisibility(View.VISIBLE);
            mDns_discover_layout.setVisibility(View.GONE);
            mywebView.setVisibility(View.GONE);
            Toast.makeText(this, "You dont have any active internet connection", Toast.LENGTH_SHORT).show();
        }
    }

   // public void ReconectWebSite(View view) {
   //     loadWebPage(mContext);
   // }

    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
     /*   getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    */
    }

    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue,
                                  final JsPromptResult result) {
            return true;
        }
    }

    public class BrowserClient extends WebViewClient {

        SwipeRefreshLayout swipeRefreshLayout;

        public BrowserClient(SwipeRefreshLayout swipeRefreshLayout) {
            this.swipeRefreshLayout = swipeRefreshLayout;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted: " + url);
            //Toast.makeText(getApplicationContext(),"onPageStarted",Toast.LENGTH_LONG).show();

            super.onPageStarted(view, url, favicon);
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            view.evaluateJavascript("setWebView();", null);
            Log.w(TAG, "setWebView() called from android");
            /*view.loadUrl(
                    """javascript:(function f() {
                      var btns = document.getElementsByTagName('button');
                      for (var i = 0, n = allElements.length; i < n; i++) {
                        if (btns[i].getAttribute('aria-label') === 'Support') {
                          btns[i].setAttribute('onclick', 'Android.onClicked()');
                        }
                      }
                    })()"""
            );*/
            swipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public void onLoadResource(WebView view, String url) {
            Log.d(TAG, "onLoadResource: " + url);
            //Toast.makeText(getApplicationContext(),"onLoadResource",Toast.LENGTH_LONG).show();

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading 1: " + url);
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            //final Uri uri = Uri.parse(url);
            //Toast.makeText(getApplicationContext(),"prova1",Toast.LENGTH_LONG).show();
            return handleUri(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d(TAG, "shouldOverrideUrlLoading 2: " + request.getUrl());
            //final Uri uri = request.getUrl();
            return handleUri(view, request.getUrl().toString());
        }

        private boolean handleUri(WebView view, String url) {
            //Toast.makeText(getApplicationContext(),"prova2",Toast.LENGTH_LONG).show();
            if(url.contains("index.htm")) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                Log.d(TAG, "handleUri index.htm matched: " + url);
                // enable or re-enable swipe to refresh page after disabling it in gamepad view
                handler.sendEmptyMessage(2);
            //return false;
            }
            else {

            }
       //     view.loadUrl(url);
     //       getSupportActionBar().hide();
            //final String host = uri.getHost();
            //final String scheme = uri.getScheme();
            // Based on some condition you need to determine if you are going to load the url
            // in your web view itself or in a browser.
            // You can use `host` or `scheme` or any part of the `uri` to decide.
            //if (/* any condition */) {
                // Returning false means that you are going to load this url in the webView itself
           //     return false;
           // } else {
                // Returning true means that you need to handle what to do with the url
                // e.g. open web page in a Browser
            //    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
           //     startActivity(intent);
            //    return true;
            //}
            return false;
        }

        @Override
        public WebResourceResponse shouldInterceptRequest ( WebView view, String url) {
            Log.d(TAG, "shouldInterceptRequest 1: " + url);
            //Toast.makeText(getApplicationContext(),"prova3",Toast.LENGTH_LONG).show();
            handleUri(view, url);

            // if (url.contains(".css")) {
           //     return getCssWebResourceResponseFromAsset();
            //} else {
                return super.shouldInterceptRequest(view, url);
            //}
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          WebResourceRequest request) {
            Log.d(TAG, "shouldInterceptRequest 2: " + request.getUrl());
            //Toast.makeText(getApplicationContext(),"prova4",Toast.LENGTH_LONG).show();
            handleUri(view, request.getUrl().toString());
            return shouldInterceptRequest(view, request.getUrl().toString());
        }

    }

    @Override
    public void onBackPressed(){
        if(mywebView.canGoBack()){
            //myWebView = (WebView) findViewById(R.id.webview);
            WebBackForwardList mWebBackForwardList = mywebView.copyBackForwardList();
            if (mWebBackForwardList.getCurrentIndex() > 0) {
                String historyUrl = mWebBackForwardList.getItemAtIndex(mWebBackForwardList.getCurrentIndex() - 1).getUrl();
                if(historyUrl.contains("index.htm")) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            }
            mywebView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }

    private static int count = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        Log.v("onActivity", "Activity created ");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.v("onActivity", "Activity started ");
        if(background){
            background = false;
            Log.v("activityFocus", "Activity came in foreground ");
            Toast.makeText(getApplicationContext(), "Foreground", Toast.LENGTH_SHORT).show();
            //loadWebPage(mDnsDiscover.getIp());
            // reload the activity
            startActivity(getIntent());
            overridePendingTransition(0, 0);

        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        count++;
        Log.v("onActivity", "Activity resumed ");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        count--;
        Log.v("onActivity", "Activity paused ");

    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.v("onActivity", "Activity stopped ");
        if(count==0){
            Log.v("activityFocus", "Activity is in background ");
            Toast.makeText(getApplicationContext(), "Background", Toast.LENGTH_SHORT).show();
            background=true;
            // stop mDns if active, it consumes bandwidth even if APP is paused in background
            mDnsDiscover.discoverStop();
            // close websocket, no need to keep connection and wasting resources and bandwidth
            mywebView.evaluateJavascript("wsStop();", null);
            // load and empty page in webview to avoid anything running in background
            // Reload whole page if resumed, dirty but simple
            mywebView.loadUrl("about:blank");
            //terminate activity
            finish();
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        Log.v("onActivity", "Activity SaveInstanceState ");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.v("onActivity", "Activity Destroyed ");
    }
}