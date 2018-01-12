
package online.sniper.webkit;

import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class JsCallJava {

    public static final String TAG = JsCallJava.class.getSimpleName();

    // Javascript信息
    public static final String JS_PART_1 = "javascript:(function(b){if(b.%s!=null){console.log(\"%s was injected\");return;}console.log(\"%s initialization begin\");";
    public static final String JS_PART_2 = "var a={};";
    public static final String JS_PART_3 = "a.%s=";
    public static final String JS_PART_4 = "function(){var f=Array.prototype.slice.call(arguments,0);if(f.length<1){throw\"%s:miss method name\"}var e=[];for(var h=1;h<f.length;h++){var c=f[h];var j=typeof c;e[e.length]=j;}var g=JSON.parse(prompt(JSON.stringify({object:\"%s\",method:f.shift(),types:e,args:f})));if(g.code!=200){throw\"%s:code=\"+g.code+\", message=\"+g.result}return g.result};";
    public static final String JS_PART_5 = "Object.getOwnPropertyNames(a).forEach(function(d){var c=a[d];if(typeof c===\"function\"){a[d]=function(){return c.apply(a,[d].concat(Array.prototype.slice.call(arguments,0)))}}});";
    public static final String JS_PART_6 = "b.%s=a;a.%s();console.log(\"%s initialization end\")})(window);";

    // 方法信息的键
    public static final String KEY_OBJECT = "object";
    public static final String KEY_METHOD = "method";
    public static final String KEY_TYPES = "types";
    public static final String KEY_ARGS = "args";

    // 注入成功的回调方法
    public static final String METHOD_ON_INJECTED = "__onInjected__";

    // 返回值格式
    public static final String RETURN_RESULT_FORMAT = "{\"code\": %d, \"result\": %s}";

    private final Object mInjectedObject;
    private final String mInjectedName;
    private final HashMap<String, Method> mMethodsMap;
    private final HashMap<String, Method> mMethodsMap2;
    private final String mJavascript;
    private volatile boolean mInjected;
    private Method mOnInjectedMethod = null;

    /** 过滤掉所有来自{@link Object}的方法，重写后的共有方法除外 */
    private static final HashSet<Method> sMethodFilter = new HashSet<Method>();

    static {
        Method[] methods = Object.class.getMethods();
        for (Method method : methods) {
            sMethodFilter.add(method);
        }
    }

    /**
     * 注入接口类
     *
     * @param name   注入接口的名称，不允许为null
     * @param object 注入接口的类名，不允许为null
     */
    public JsCallJava(Object object, String name) {
        if (object == null || name == null) {
            throw new IllegalArgumentException();
        }

        mInjectedName = name;
        mInjectedObject = object;
        mMethodsMap = new HashMap<String, Method>();
        mMethodsMap2 = new HashMap<String, Method>();

        //  getMethods会获得所有继承与非继承的方法
        Method[] methods = object.getClass().getMethods();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.getDefault(), JS_PART_1, name, name, name));
        sb.append(JS_PART_2);
        sb.append(String.format(Locale.getDefault(), JS_PART_3, METHOD_ON_INJECTED));
        for (Method method : methods) {
            if (sMethodFilter.contains(method)) {
                continue;
            }
            mMethodsMap.put(getJavaMethodSign(method), method);
            mMethodsMap2.put(getJavaMethodSign2(method), method);
            sb.append(String.format(Locale.getDefault(), JS_PART_3, method.getName()));
            if ("onInjected".equals(method.getName())) {
                mOnInjectedMethod = method;
            }
        }
        sb.append(String.format(Locale.getDefault(), JS_PART_4, name, name, name));
        sb.append(JS_PART_5);
        sb.append(String.format(Locale.getDefault(), JS_PART_6, name, METHOD_ON_INJECTED, name));
        mJavascript = sb.toString();
        WebViewDebugger.d(TAG, mJavascript);
    }

    /**
     * 获取返回值
     *
     * @param in        输入参数
     * @param stateCode 返回码 成功：200；否则：500
     * @param result    输出结果
     * @return
     */
    public static String getReturn(Object in, int stateCode, Object result) {
        String outResult;
        if (result == null) {
            outResult = "null";
        } else if (result instanceof String) {
            result = ((String) result).replace("\"", "\\\"");
            outResult = "\"" + result + "\"";
        } else {
            try {
                outResult = String.valueOf(result);
            } catch (Exception e) {
                stateCode = 500;
                outResult = "json encode result error:" + e.getMessage();
            }
        }
        String ret = String.format(Locale.getDefault(), RETURN_RESULT_FORMAT, stateCode, outResult);
        WebViewDebugger.d(TAG, String.format(Locale.getDefault(), "in:%s, out:%s", in == null ? "null" : in.toString(), ret));
        return ret;
    }

    /**
     * 验证方法信息格式是否合法，如果合法返回JSON对象，否则返回null
     *
     * @param json 字符串形式的方法信息
     * @return 方法信息格式合法，返回JSON对象；否则返回null
     */
    public static JSONObject getValidMethodInfo(String json) {
        JSONObject ret = null;

        try {
            ret = new JSONObject(json);
        } catch (Exception e) {
            return null;
        }

        if (ret.length() != 4) {
            return null;
        } else if (ret.isNull(KEY_OBJECT)) {
            return null;
        } else if (ret.isNull(KEY_METHOD)) {
            return null;
        } else if (ret.isNull(KEY_TYPES)) {
            return null;
        } else if (ret.isNull(KEY_ARGS)) {
            return null;
        } else {
            return ret;
        }
    }

    /**
     * 获取注入接口的名称
     */
    public String getInjectedName() {
        return mInjectedName;
    }

    /**
     * 获取注入的类
     */
    public Object getInjectedObject() {
        return mInjectedObject;
    }

    /**
     * 获取注入接口的JS代码
     */
    public String getJavascript() {
        return mJavascript;
    }

    /**
     * 重置标识
     */
    public void clearFlags() {
        mInjected = false;
    }

    /**
     * 当注入成功时调用
     */
    void onInjected() {
        mInjected = true;

        try {
            if (mOnInjectedMethod != null) {
                mOnInjectedMethod.invoke(mInjectedObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断是否已经注入成功
     */
    public boolean isInjected() {
        return mInjected;
    }

    /**
     * 获取方法签名sign
     *
     * @param method Method
     * @return 签名sign
     */
    private String getJavaMethodSign(Method method) {
        String sign = method.getName();
        Class<?>[] argsTypes = method.getParameterTypes();
        for (int i = 0; i < argsTypes.length; i++) {
            Class<?> clazz = argsTypes[i];
            if (clazz == int.class || clazz == long.class || clazz == float.class || clazz == double.class) {
                sign += "_N";
            } else if (clazz == boolean.class) {
                sign += "_B";
            } else if (clazz == String.class) {
                sign += "_S";
            } else if (clazz == JSONObject.class) {
                sign += "_O";
            } else {
                sign += "_P";
            }
        }
        return sign;
    }

    /**
     * 获取方法签名sign2
     *
     * @param method Method
     * @return 签名sign2
     */
    private String getJavaMethodSign2(Method method) {
        Class<?>[] argsTypes = method.getParameterTypes();
        return getJavaMethodSign2(method.getName(), argsTypes == null ? 0 : argsTypes.length);
    }

    /**
     * 获取方法签名sign2
     * @param methodName 方法名
     * @param argsCount 参数个数
     * @return 签名sign2
     */
    private String getJavaMethodSign2(String methodName, int argsCount) {
        return methodName + "__" + argsCount + "__";
    }

    /**
     * 调用接口接口
     *
     * @param view WebView
     * @param json 方法信息
     * @return 返回值
     */
    public String call(WebView view, String json) {
        if (view == null || json == null) {
            return getReturn(null, 500, "call data empty");
        }
        JSONObject methodInfo = getValidMethodInfo(json);
        if (methodInfo == null) {
            return getReturn(json, 500, "invalid method info");
        }
        return call(view, methodInfo);
    }

    /**
     * 调用注入接口
     *
     * @param view WebView
     * @param json 方法信息
     * @return 返回值
     */
    public String call(WebView view, JSONObject json) {
        if (view == null || json == null) {
            return getReturn(null, 500, "call data empty");
        }

        try {
            String methodName = json.getString(KEY_METHOD);
            if (METHOD_ON_INJECTED.contains(methodName)) {
                onInjected();
                return getReturn(json, 200, "");
            }

            JSONArray argsTypes = json.getJSONArray(KEY_TYPES);
            JSONArray argsValues = json.getJSONArray(KEY_ARGS);
            String sign = methodName;
            int argsCount = argsTypes.length();

            // 先匹配sign
            for (int i = 0; i < argsCount; i++) {
                String currentType = argsTypes.optString(i);
                if ("number".equals(currentType)) {
                    sign += "_N";
                } else if ("boolean".equals(currentType)) {
                    sign += "_B";
                } else if ("string".equals(currentType)) {
                    sign += "_S";
                } else if ("object".equals(currentType)) {
                    sign += "_O";
                } else {
                    sign += "_P";
                }
            }
            Method method = mMethodsMap.get(sign);
            // 若sign不匹配，则匹配sign2
            if (method == null) {
                String sign2 = getJavaMethodSign2(methodName, argsCount);
                method = mMethodsMap2.get(sign2);
            }

            // 方法匹配失败
            if (method == null) {
                return getReturn(json, 500, "not found method(" + methodName + ") with valid parameters");
            }

            Class<?>[] methodTypes = method.getParameterTypes();
            Object[] values = new Object[argsCount];
            for (int i = 0; methodTypes != null && i < methodTypes.length; i++) {
                if (methodTypes[i] == int.class) {
                    values[i] = argsValues.isNull(i) ? (int) 0 : argsValues.getInt(i);
                } else if (methodTypes[i] == long.class) {
                    values[i] = argsValues.isNull(i) ? (long) 0 : Long.parseLong(argsValues.getString(i));
                } else if (methodTypes[i] == float.class) {
                    values[i] = argsValues.isNull(i) ? (float) 0 : (float) argsValues.getDouble(i);
                } else if (methodTypes[i] == double.class) {
                    values[i] = argsValues.isNull(i) ? (double) 0 : argsValues.getDouble(i);
                } else if (methodTypes[i] == boolean.class) {
                    values[i] = argsValues.isNull(i) ? false : argsValues.getBoolean(i);
                } else if (methodTypes[i] == String.class) {
                    values[i] = argsValues.isNull(i) ? null : argsValues.getString(i);
                } else if (methodTypes[i] == JSONObject.class) {
                    values[i] = argsValues.isNull(i) ? null : argsValues.getJSONObject(i);
                } else {
                    values[i] = null;
                }
            }

            return getReturn(json, 200, method.invoke(mInjectedObject, values));
        } catch (Exception e) {
            // 优先返回详细的错误信息
            if (e.getCause() != null) {
                return getReturn(json, 500, "method execute error:" + e.getCause().getMessage());
            }
            return getReturn(json, 500, "method execute error:" + e.getMessage());
        }
    }
}
