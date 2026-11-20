package com.qiqi.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String PREFS = "qiqi_prefs";
    private static final String KEY_URL = "server_url";
    private static final String DEFAULT_URL = "https://relatives-dpi-brunswick-committed.trycloudflare.com";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 申请录音权限
        if (Build.VERSION.SDK_INT >= 23
                && checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }

        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);   // 允许自动播放（她发语音）
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);       // 用 Service Worker / 缓存
        s.setUserAgentString(s.getUserAgentString() + " QiqiApp/1.0");

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                // 自动授权麦克风（语音录音）
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        request.grant(request.getResources());
                    }
                });
            }
        });

        String savedUrl = getSharedPreferences(PREFS, MODE_PRIVATE).getString(KEY_URL, null);
        if (savedUrl == null || savedUrl.trim().isEmpty()) {
            showUrlDialog();
        } else {
            webView.loadUrl(savedUrl);
        }
    }

    private void showUrlDialog() {
        final EditText input = new EditText(this);
        input.setText(DEFAULT_URL);
        input.setHint("https://你的服务器地址");
        new AlertDialog.Builder(this)
                .setTitle("琪琪 · 服务器地址")
                .setMessage("首次使用请填写琪琪服务地址（电脑上 8787 服务经 HTTPS 隧道暴露的地址）：")
                .setView(input)
                .setPositiveButton("开始", (dialog, which) -> {
                    String u = input.getText().toString().trim();
                    if (!u.startsWith("http://") && !u.startsWith("https://")) {
                        u = "https://" + u;
                    }
                    getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_URL, u).apply();
                    webView.loadUrl(u);
                })
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
