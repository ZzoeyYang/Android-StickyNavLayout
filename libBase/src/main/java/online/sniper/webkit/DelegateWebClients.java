
package online.sniper.webkit;

import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage.QuotaUpdater;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 负责注入接口 一个WebView要想使用该类防止注入，就必须同时调用它的getWebViewClient()和getWebChromeClient().
 * 
 * @author wangpeihe
 *
 */
@SuppressWarnings("deprecation")
public final class DelegateWebClients {

    public static final String TAG = DelegateWebClients.class.getSimpleName();

    private final Map<String, JsCallJava> mJsCallJavas = new HashMap<String, JsCallJava>();
    private DelegateWebViewClient mDelegateWebViewClient;
    private DelegateWebChromeClient mDelegateWebChromeClient;

    public DelegateWebClients() {
    }

    /**
     * 添加一个JavascriptInterface
     * 
     * @param injectedObject
     *            注入接口类
     * @param injectedName
     *            注入接口名称
     */
    public void addJavascriptInterface(Object injectedObject, String injectedName) {
        if (injectedName == null || injectedObject == null) {
            return;
        }

        mJsCallJavas.put(injectedName, new JsCallJava(injectedObject, injectedName));
    }

    /**
     * 删除一个JavascriptInterface
     * 
     * @param injectedName
     *            注入接口名称
     */
    public void removeJavascriptInterface(String injectedName) {
        if (injectedName == null) {
            return;
        }

        mJsCallJavas.remove(injectedName);
    }

    /**
     * 清除全部的JavascriptInterface
     */
    public void clearJavascriptInterfaces() {
        mJsCallJavas.clear();
    }

    /**
     * 获取负责注入接口的WebViewClient
     * 
     * @return
     */
    public DelegateWebViewClient getDelegateWebViewClient() {
        if (mDelegateWebViewClient == null) {
            mDelegateWebViewClient = new DelegateWebViewClient();
        }
        return mDelegateWebViewClient;
    }

    /**
     * 获取负责调用注入接口的WebChromeClient
     * 
     * @return
     */
    public DelegateWebChromeClient getDelegateWebChromeClient() {
        if (mDelegateWebChromeClient == null) {
            mDelegateWebChromeClient = new DelegateWebChromeClient();
        }
        return mDelegateWebChromeClient;
    }

    /**
     * 清空注入接口的标识
     * 
     * @param view
     */
    private void clearJavascriptInterfacesFlags(WebView view) {
        Collection<JsCallJava> jsCallJavas = mJsCallJavas.values();
        for (JsCallJava jsCallJava : jsCallJavas) {
            if (jsCallJava != null) {
                jsCallJava.clearFlags();
            }
        }
    }

    /**
     * 加载注入接口
     * 
     * @param view
     */
    private void loadJavascriptInterfaces(WebView view) {
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            view.removeJavascriptInterface("searchBoxJavaBridge_");
            view.removeJavascriptInterface("accessibility");
            view.removeJavascriptInterface("accessibilityTraversal");
        }

        Collection<JsCallJava> jsCallJavas = mJsCallJavas.values();
        for (JsCallJava jsCallJava : jsCallJavas) {
            if (jsCallJava != null && !jsCallJava.isInjected()) {
                String js = jsCallJava.getJavascript();
                if (js != null && js.length() != 0) {
                    view.loadUrl(js);
                }
            }
        }
    }

    /**
     * 调用注入接口
     * 
     * @param view
     * @param json
     * @return
     */
    private String callJavascriptInterface(WebView view, JSONObject json) {
        if (view == null || json == null) {
            return JsCallJava.getReturn(null, 500, "call data empty");
        }

        try {
            String injectedName = json.getString(JsCallJava.KEY_OBJECT);
            JsCallJava jsCallJava = mJsCallJavas.get(injectedName);
            if (jsCallJava == null) {
                return JsCallJava.getReturn(json, 500, "class no found");
            }
            return jsCallJava.call(view, json);
        } catch (Exception e) {
            // 优先返回详细的错误信息
            if (e.getCause() != null) {
                return JsCallJava.getReturn(json, 500, "method execute error:" + e.getCause().getMessage());
            }
            return JsCallJava.getReturn(json, 500, "method execute error:" + e.getMessage());
        }
    }

    /**
     * 负责注入接口的类.
     * 继承此类时，若重写onPageStarted()和onLoadResource()，则必须调用super.onPageStarted
     * ()和onLoadResource()，还要重写InjectedWebClient的getWebViewClient()
     * 
     * @author wangpeihe
     *
     */
    public final class DelegateWebViewClient extends WebViewClient {
        private WebViewClient mWebViewClient;

        protected DelegateWebViewClient() {
        }

        public void setWebViewClient(WebViewClient webViewClient) {
            mWebViewClient = webViewClient;
        }

        public void onPageStarted(final WebView view, final String url, Bitmap favicon) {
            // 清除标识
            clearJavascriptInterfacesFlags(view);
            // 成功率非常低，可以放弃加载
            loadJavascriptInterfaces(view);

            if (mWebViewClient != null) {
                mWebViewClient.onPageStarted(view, url, favicon);
            }
        }

        public void onLoadResource(WebView view, String url) {
            // 成功就在眼前，尽情加载吧
            loadJavascriptInterfaces(view);

            if (mWebViewClient != null) {
                mWebViewClient.onLoadResource(view, url);
            }
        }

        public void onPageFinished(WebView view, String url) {
            // 我来收拾残局，保证加载成功
            loadJavascriptInterfaces(view);

            if (mWebViewClient != null) {
                mWebViewClient.onPageFinished(view, url);
            }
        }

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if (mWebViewClient != null) {
                mWebViewClient.onReceivedSslError(view, handler, error);
            }
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (mWebViewClient != null) {
                return mWebViewClient.shouldOverrideUrlLoading(view, url);
            }
            return false;
        }

        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            if (android.os.Build.VERSION.SDK_INT >= 11 && mWebViewClient != null) {
                return mWebViewClient.shouldInterceptRequest(view, url);
            }
            return null;
        }

        public void onTooManyRedirects(WebView view, Message cancelMsg, Message continueMsg) {
            if (mWebViewClient != null) {
                mWebViewClient.onTooManyRedirects(view, cancelMsg, continueMsg);
            }
        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if (mWebViewClient != null) {
                mWebViewClient.onReceivedError(view, errorCode, description, failingUrl);
            }
        }

        public void onFormResubmission(WebView view, Message dontResend, Message resend) {
            if (mWebViewClient != null) {
                mWebViewClient.onFormResubmission(view, dontResend, resend);
            }
        }

        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            loadJavascriptInterfaces(view);
            if (mWebViewClient != null) {
                mWebViewClient.doUpdateVisitedHistory(view, url, isReload);
            }
        }

        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            if (mWebViewClient != null) {
                mWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
            }
        }

        public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
            if (mWebViewClient != null) {
                return mWebViewClient.shouldOverrideKeyEvent(view, event);
            }
            return false;
        }

        public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
            if (mWebViewClient != null) {
                mWebViewClient.onUnhandledKeyEvent(view, event);
            }
        }

        public void onScaleChanged(WebView view, float oldScale, float newScale) {
            if (mWebViewClient != null) {
                mWebViewClient.onScaleChanged(view, oldScale, newScale);
            }
        }

        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            if (android.os.Build.VERSION.SDK_INT >= 12 && mWebViewClient != null) {
                mWebViewClient.onReceivedLoginRequest(view, realm, account, args);
            }
        }

    }

    /**
     * 负责调用注入接口的类. 继承此类时，若重写onJsPrompt()，则必须调用super.onJsPrompt()
     * ,还要重写InjectedWebClient的getWebChromeClient()
     * 
     * @author wangpeihe
     *
     */
    public final class DelegateWebChromeClient extends WebChromeClient {

        private WebChromeClient mWebChromeClient;

        protected DelegateWebChromeClient() {
        }

        public void setWebChromeClient(WebChromeClient webChromeClient) {
            mWebChromeClient = webChromeClient;
        }

        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            JSONObject json = JsCallJava.getValidMethodInfo(message);
            if (json != null) {
                result.confirm(callJavascriptInterface(view, json));
                return true;
            } else if (mWebChromeClient != null) {
                return mWebChromeClient.onJsPrompt(view, url, message, defaultValue, result);
            } else {
                return false;
            }
        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            WebViewDebugger.d(TAG, "message=" + consoleMessage.message());
            WebViewDebugger.d(TAG, "file=" + consoleMessage.sourceId());
            WebViewDebugger.d(TAG, "line=" + consoleMessage.lineNumber());
            WebViewDebugger.d(TAG, "level=" + consoleMessage.messageLevel().name());

            if (mWebChromeClient != null) {
                return mWebChromeClient.onConsoleMessage(consoleMessage);
            }
            return false;
        }

        public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
            if (mWebChromeClient != null) {
                return mWebChromeClient.onJsAlert(view, url, message, result);
            }
            return false;
        }

        private boolean isInjected = false;

        public void onProgressChanged(WebView view, int newProgress) {
            if (newProgress <= 25) {
                isInjected = false;
            } else if (!isInjected) {
                loadJavascriptInterfaces(view);
                isInjected = true;
            }

            if (mWebChromeClient != null) {
                mWebChromeClient.onProgressChanged(view, newProgress);
            }
        }

        public void onReceivedTitle(WebView view, String title) {
            loadJavascriptInterfaces(view);

            if (mWebChromeClient != null) {
                mWebChromeClient.onReceivedTitle(view, title);
            }
        }

        public void onReceivedIcon(WebView view, Bitmap icon) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onReceivedIcon(view, icon);
            }
        }

        public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onReceivedTouchIconUrl(view, url, precomposed);
            }
        }

        public void onShowCustomView(View view, CustomViewCallback callback) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onShowCustomView(view, callback);
            }
        }

        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onShowCustomView(view, requestedOrientation, callback);
            }
        }

        public void onHideCustomView() {
            if (mWebChromeClient != null) {
                mWebChromeClient.onHideCustomView();
            }
        }

        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            if (mWebChromeClient != null) {
                return mWebChromeClient.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            }
            return false;
        }

        public void onRequestFocus(WebView view) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onRequestFocus(view);
            }
        }

        public void onCloseWindow(WebView window) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onCloseWindow(window);
            }
        }

        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            if (mWebChromeClient != null) {
                return mWebChromeClient.onJsConfirm(view, url, message, result);
            }
            return false;
        }

        public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
            if (mWebChromeClient != null) {
                return mWebChromeClient.onJsBeforeUnload(view, url, message, result);
            }
            return false;
        }

        public void onGeolocationPermissionsShowPrompt(String origin, Callback callback) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        }

        public void onGeolocationPermissionsHidePrompt() {
            if (mWebChromeClient != null) {
                mWebChromeClient.onGeolocationPermissionsHidePrompt();
            }
        }

        public boolean onJsTimeout() {
            if (mWebChromeClient != null) {
                return mWebChromeClient.onJsTimeout();
            }
            return true;
        }

        public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onConsoleMessage(message, lineNumber, sourceID);
            }
        }

        public Bitmap getDefaultVideoPoster() {
            if (mWebChromeClient != null) {
                return mWebChromeClient.getDefaultVideoPoster();
            }
            return null;
        }

        public View getVideoLoadingProgressView() {
            if (mWebChromeClient != null) {
                return mWebChromeClient.getVideoLoadingProgressView();
            }
            return null;
        }

        public void getVisitedHistory(ValueCallback<String[]> callback) {
            if (mWebChromeClient != null) {
                mWebChromeClient.getVisitedHistory(callback);
            }
        }

        public void onExceededDatabaseQuota(String url, String databaseIdentifier, long quota, long estimatedDatabaseSize, long totalQuota, QuotaUpdater quotaUpdater) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onExceededDatabaseQuota(url, databaseIdentifier, quota, estimatedDatabaseSize, totalQuota, quotaUpdater);
            }
        }

        public void onReachedMaxAppCacheSize(long requiredStorage, long quota, QuotaUpdater quotaUpdater) {
            if (mWebChromeClient != null) {
                mWebChromeClient.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
            }
        }

    }

}
