package com.myraa.ai;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.webkit.*;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import java.util.*;

public class MainActivity extends Activity {
    WebView webView;
    static final int REQ = 9001;
    static final String PREF = "myraa";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(android.graphics.Color.rgb(5,5,9));
        getWindow().setNavigationBarColor(android.graphics.Color.rgb(5,5,9));

        webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);
        s.setLoadWithOverviewMode(false);
        s.setUseWideViewPort(false);
        s.setTextZoom(100);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();
        webView.setWebViewClient(new WebViewClientCompat() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, android.webkit.WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                return loader.shouldInterceptRequest(Uri.parse(url));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if (u != null && "https".equalsIgnoreCase(u.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                    return true;
                }
                return false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> {
                    ArrayList<String> allow = new ArrayList<>();
                    for (String r : request.getResources()) {
                        if (PermissionRequest.RESOURCE_AUDIO_CAPTURE.equals(r) && checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) allow.add(r);
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(r) && checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) allow.add(r);
                    }
                    if (!allow.isEmpty()) request.grant(allow.toArray(new String[0])); else request.deny();
                });
            }
        });
        webView.addJavascriptInterface(new Bridge(), "MYRAAAndroid");
        setContentView(webView);
        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html");
        requestPermissionsSafe();
    }

    void requestPermissionsSafe() {
        List<String> p = new ArrayList<>();
        String[] a = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA, Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS,
                Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.POST_NOTIFICATIONS};
        for (String x : a) if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(x) != PackageManager.PERMISSION_GRANTED) p.add(x);
        if (!p.isEmpty() && Build.VERSION.SDK_INT >= 23) requestPermissions(p.toArray(new String[0]), REQ);
    }

    void open(String a) { try { startActivity(new Intent(a)); } catch (Exception ignored) {} }
    void open(String a, String d) { try { startActivity(new Intent(a, Uri.parse(d))); } catch (Exception ignored) {} }

    public class Bridge {
        @JavascriptInterface public String platform() { return "android"; }
        @JavascriptInterface public String getApiBaseUrl() { return getSharedPreferences(PREF,0).getString("apiBase", ""); }
        @JavascriptInterface public void setApiBaseUrl(String u) { getSharedPreferences(PREF,0).edit().putString("apiBase", u == null ? "" : u.trim()).apply(); }
        @JavascriptInterface public void requestPermissions() { runOnUiThread(() -> requestPermissionsSafe()); }
        @JavascriptInterface public void openAccessibilitySettings() { open(Settings.ACTION_ACCESSIBILITY_SETTINGS); }
        @JavascriptInterface public void openNotificationAccessSettings() { open("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"); }
        @JavascriptInterface public void openOverlaySettings() { open(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:" + getPackageName()); }
        @JavascriptInterface public void openUsageAccessSettings() { open("android.settings.action.USAGE_ACCESS_SETTINGS"); }
        @JavascriptInterface public void openBatterySettings() { open(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS); }
        @JavascriptInterface public boolean overlayEnabled() { return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(MainActivity.this); }
        @JavascriptInterface public boolean batteryOptimizationIgnored() { PowerManager p=(PowerManager)getSystemService(POWER_SERVICE); return Build.VERSION.SDK_INT<23 || p.isIgnoringBatteryOptimizations(getPackageName()); }
        @JavascriptInterface public boolean accessibilityEnabled() { String x=Settings.Secure.getString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES); return x!=null && x.contains(MyraaAccessibilityService.class.getName()); }
        @JavascriptInterface public boolean notificationAccessEnabled() { String x=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners"); return x!=null && x.contains(getPackageName()); }
        @JavascriptInterface public void startBackgroundAssistant() { try { if(Build.VERSION.SDK_INT>=26) startForegroundService(new Intent(MainActivity.this,MyraaForegroundService.class)); else startService(new Intent(MainActivity.this,MyraaForegroundService.class)); } catch(Exception ignored) {} }
        @JavascriptInterface public void stopBackgroundAssistant() { stopService(new Intent(MainActivity.this,MyraaForegroundService.class)); }
        @JavascriptInterface public boolean performGlobalBack() { return MyraaAccessibilityService.performGlobalBack(); }
        @JavascriptInterface public boolean performHome() { return MyraaAccessibilityService.performHome(); }
        @JavascriptInterface public boolean performRecents() { return MyraaAccessibilityService.performRecents(); }
    }

    @Override protected void onDestroy() { if (webView != null) { webView.stopLoading(); webView.destroy(); } super.onDestroy(); }
    @Override public void onBackPressed() { if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed(); }
}
