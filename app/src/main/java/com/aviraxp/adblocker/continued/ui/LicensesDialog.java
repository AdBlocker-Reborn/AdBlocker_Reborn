package com.aviraxp.adblocker.continued.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class LicensesDialog extends AlertDialog.Builder {

    private final Context ctx;

    LicensesDialog(Context context) {
        super(context);
        ctx = context;
        init();
    }

    private void init() {
        WebView view = new WebView(ctx);
        view.loadUrl("file:///android_asset/licenses.html");
        view.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && url.startsWith("http")) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        setView(view);
    }
}