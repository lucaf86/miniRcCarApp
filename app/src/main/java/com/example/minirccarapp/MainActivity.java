package com.example.minirccarapp;

import static android.content.ContentValues.TAG;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.intellij.lang.annotations.RegExp;

import java.net.URL;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    private WebView mywebView;

    private NsdManager mNsdManager;

    public String espIPAddr = "null";

    public String espCarFound() {
        return espIPAddr;
    }


    NsdManager.ResolveListener mResolveListener;

    //private NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
    void initializeResolveListener() {
        Log.e(TAG, "initializeResolveListener ... ");
        mResolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onResolveFailed (NsdServiceInfo serviceInfo,int errorCode){
            // Called when the resolve fails.  Use the error code to debug.
            Log.e(TAG, "Resolve failed" + errorCode);
        }

        @Override
        public void onServiceResolved (NsdServiceInfo serviceInfo){
            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

              /*  if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }*/
            NsdServiceInfo service = serviceInfo;
            int port = service.getPort();
            InetAddress host = service.getHost(); // getHost() will work now
            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

            if(host.getHostAddress().toString().startsWith("/")) {
                Log.d(TAG,"IP: " + host.getHostAddress().toString().substring(1) );
                espIPAddr = host.getHostAddress().toString().substring(1);
            }
            else {
                espIPAddr = host.getHostAddress().toString();
            }
            Log.d(TAG,"host IP: " + espIPAddr );

           // mywebView.loadUrl(service.getHost().toString());

        }
    };
        Log.e(TAG, "initializeResolveListener ... END");
}



    // Instantiate a new DiscoveryListener
    NsdManager.DiscoveryListener mDiscoveryListener;

        // private NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
    void initializeListener() {
        Log.e(TAG, "initializeListener ... ");
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            String TAG = "NSDFINDER";
            //NsdManager mNsdManager;
            //NsdManager.DiscoveryListener mDiscoveryListener;
            String mServiceName;
            //String SERVICE_TYPE = "_http._tcp.";
            String SERVICE_TYPE = "_espcar._tcp.";
            String serviceName;
            int mDiscoveryActive = 0;
            //NsdManager.ResolveListener mResolveListener;
            NsdServiceInfo mService;
            int mServiceport;
            InetAddress mServicehostAdress;

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.

                mNsdManager.resolveService(service, mResolveListener);
                //mNsdManager.stopServiceDiscovery(mDiscoveryListener)


                Log.d(TAG, "Service discovery success :: " + service);
                int mServiceport = service.getPort();
                Log.d(TAG, "getAttributes :: " + service.getAttributes());
                InetAddress mServicehostAdress = service.getHost();
                Log.d(TAG, "Service Port: " + mServiceport + " Adresse: " + mServicehostAdress);
                //Toast.makeText(getApplicationContext(),"Service Port: "+mServiceport+" Adresse: "+mServicehostAdress,Toast.LENGTH_LONG).show();

                try {
                    InetAddress inetAddr = InetAddress.getByName("mini_rc_car.local");


                    byte[] addr = inetAddr.getAddress();

                    // Convert to dot representation
                    String ipAddr = "";
                    for (int i = 0; i < addr.length; i++) {
                        if (i > 0) {
                            ipAddr += ".";
                        }
                        ipAddr += addr[i] & 0xFF;
                    }

                    Log.d(TAG, "IP Address: " + ipAddr);
                } catch (UnknownHostException e) {
                    Log.d(TAG, "Host not found: " + e.getMessage());
                }

         /*   if (!service.getServiceType().equals(SERVICE_TYPE)) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
            } else if (service.getServiceName().equals(serviceName)) {
                // The name of the service tells the user what they'd be
                // connecting to. It could be "Bob's Chat App".
                Log.d(TAG, "Same machine: " + serviceName);
            } else if (service.getServiceName().contains("NsdChat")){
                NsdManager.ResolveListener resolveListener;
                mNsdManager.resolveService(service, resolveListener);
            }*/

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
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
        Log.e(TAG, "initializeListener ... END");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {




        super.onCreate(savedInstanceState);
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
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener()
                {

                    @Override
                    public void onSystemUiVisibilityChange(int visibility)
                    {
                        if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        {
                            decorView.setSystemUiVisibility(flags);
                        }
                    }
                });

        Context mContext = getApplicationContext();
        //String SERVICE_TYPE = "_http._tcp.";
        String SERVICE_TYPE = "_espcar._tcp.";

        WifiManager wifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("multicastLock");
        multicastLock.setReferenceCounted(true);
        multicastLock.acquire();


        mNsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);


        initializeResolveListener();
        initializeListener();


        mNsdManager.discoverServices(SERVICE_TYPE, mNsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);

        //multicastLock.release(); // release after browsing

        mywebView = (WebView) findViewById(R.id.webview);
        mywebView.setWebChromeClient(new WebChromeClient());
        //mywebView.setWebViewClient(new WebViewClient());
        mywebView.setWebViewClient(new mywebClient());
        String espIp;
        do{
            espIp = espCarFound();
        }while(espIp == "null");
        Log.e(TAG, "esp car found IP: " + espIp);
        //mywebView.loadUrl("http://192.168.1.76");
        WebSettings webSettings = mywebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //mywebView.addJavascriptInterface(AndroidJSInterface, "Android");
        mywebView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void webViewGamepadViewSet() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                Log.d("webViewGamepadViewSet", "webViewGamepadViewSet() called from JS");
            }
        }, "Android");


        mywebView.loadUrl("http://" + espIp);
        //mywebView.evaluateJavascript("setWebView();", null);
        //Log.w(TAG, "setWebView() called from android");
    }

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


    public class mywebClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "onPageStarted: " + url);
            //Toast.makeText(getApplicationContext(),"onPageStarted",Toast.LENGTH_LONG).show();

            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
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
            final Uri uri = Uri.parse(url);
            //Toast.makeText(getApplicationContext(),"prova1",Toast.LENGTH_LONG).show();
            return handleUri(view, url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Log.d(TAG, "shouldOverrideUrlLoading 2: " + request.getUrl());
            final Uri uri = request.getUrl();
            return handleUri(view, request.getUrl().toString());
        }

        private boolean handleUri(WebView view, String url) {
            //Toast.makeText(getApplicationContext(),"prova2",Toast.LENGTH_LONG).show();
            view.loadUrl(url);
            getSupportActionBar().hide();
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

            return shouldInterceptRequest(view, request.getUrl().toString());
        }

    }

    @Override
    public void onBackPressed(){
        if(mywebView.canGoBack()){
            mywebView.goBack();
        }
        else {
            super.onBackPressed();
        }
    }
}