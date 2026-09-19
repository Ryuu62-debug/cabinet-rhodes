package com.macompta.local;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class MainActivity extends Activity {
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(247, 241, 244));
        getWindow().setNavigationBarColor(Color.rgb(247, 241, 244));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        webView = new WebView(this);
        setContentView(webView);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);

        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setWebViewClient(new WebViewClient());

        try {
            String html = readCompressedHtml();
            webView.loadDataWithBaseURL(
                "https://local.macompta/",
                html,
                "text/html",
                "UTF-8",
                null
            );
        } catch (Exception e) {
            String msg = e.getClass().getSimpleName() + ": " + (e.getMessage() == null ? "" : e.getMessage());
            webView.loadData(
                "<html><body style='font-family:sans-serif;padding:24px'><h2>Ma Compta</h2><p>Impossible de charger l'application locale.</p><p style='color:#777;font-size:12px'>" + msg + "</p></body></html>",
                "text/html",
                "UTF-8"
            );
        }
    }

    private String readTextAsset(String name) throws Exception {
        InputStream source = getAssets().open(name);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int read;
        while ((read = source.read(temp)) != -1) {
            buffer.write(temp, 0, read);
        }
        source.close();
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8).replaceAll("\\s+", "");
    }

    private String readCompressedHtml() throws Exception {
        String encoded = readTextAsset("index.part1.b64") + readTextAsset("index.part2.b64");
        byte[] compressed = Base64.decode(encoded, Base64.NO_WRAP);

        GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(compressed));
        ByteArrayOutputStream htmlBuffer = new ByteArrayOutputStream();
        byte[] temp = new byte[4096];
        int read;
        while ((read = gzip.read(temp)) != -1) {
            htmlBuffer.write(temp, 0, read);
        }
        gzip.close();

        return new String(htmlBuffer.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
