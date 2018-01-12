
package online.sniper.webkit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class SafeWebView extends WebView {

    private DelegateWebClients mDelegateWebClients;
    private DelegateWebClients.DelegateWebViewClient mDelegateWebViewClient;
    private DelegateWebClients.DelegateWebChromeClient mDelegateWebChromeClient;

    public SafeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SafeWebView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        if (hasHoneyComb()) {
            super.removeJavascriptInterface("searchBoxJavaBridge_");
            super.removeJavascriptInterface("accessibility");
            super.removeJavascriptInterface("accessibilityTraversal");
        }

        if (!hasJellyBeanMR1()) {
            mDelegateWebClients = new DelegateWebClients();
            mDelegateWebViewClient = mDelegateWebClients.getDelegateWebViewClient();
            mDelegateWebChromeClient = mDelegateWebClients.getDelegateWebChromeClient();
            super.setWebViewClient(mDelegateWebViewClient);
            super.setWebChromeClient(mDelegateWebChromeClient);
        }
    }

    @Override
    public void setWebViewClient(WebViewClient client) {
        if (hasJellyBeanMR1()) {
            super.setWebViewClient(client);
        } else {
            mDelegateWebViewClient.setWebViewClient(client);
        }
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        if (hasJellyBeanMR1()) {
            super.setWebChromeClient(client);
        } else {
            mDelegateWebChromeClient.setWebChromeClient(client);
        }
    }

    @Override
    public void addJavascriptInterface(Object object, String name) {
        if (hasJellyBeanMR1()) {
            super.addJavascriptInterface(object, name);
        } else {
            mDelegateWebClients.addJavascriptInterface(object, name);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void removeJavascriptInterface(String name) {
        if (hasHoneyComb()) {
            super.removeJavascriptInterface(name);
        }
        if (!hasJellyBeanMR1()) {
            mDelegateWebClients.removeJavascriptInterface(name);
        }
    }

    private boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= 17;
    }

    private boolean hasHoneyComb() {
        return Build.VERSION.SDK_INT >= 11;
    }

}
