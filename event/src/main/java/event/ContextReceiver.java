package event;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;

import event.base.EventBuilder;
import event.base.EventReceiver;
import event.base.EventRegister;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2017/4/19
 * modifyDate : 2017/4/19
 * @version    : 1.0
 * desc       : android上下文接收器兼注册器；页面跳转、广播发送、服务开启的模型（核心业务模型）
 * </pre>
 */

public final class ContextReceiver implements EventReceiver<Bundle, Object>, EventRegister {

    private static final boolean DEBUG = true;

    private static final String TAG = "ContextReceiver";

    public static final int REQUEST_GO_ACTIVITY = 0;
    public static final int REQUEST_SEND_BROADCAST = 1;
    public static final int REQUEST_START_SERVICE = 2;


    //如果调用startActivityForResult,则此项设置为true
    public static final String KEY_START_FOR_RESULT = "intent_for_result";
    //如果调用如果调用startActivityForResult，需要填写一个requestCode作为请求码
    public static final String KEY_REQUEST_CODE = "intent_request_code";

    //直接从requestBundle中过去用户定义的intent
    public static final String KEY_INTENT = "intent_intent";

/*程序自动生成一个需要发送的intent*/
    //获取所要传递的数据
    public static final String KEY_BUNDLE = "intent_bundle";
    //获取跳转组件的class类实例
    public static final String KEY_CLASS = "intent_class";
    //获取需要设置的行为字符串
    public static final String KEY_ACTION = "intent_action";
    //获取需要设置的URI
    public static final String KEY_DATA = "intent_data";
    //获取需要设置的mimeType
    public static final String KEY_TYPE = "intent_type";
    //获取需要设置的category
    public static final String KEY_CATEGORY = "intent_category";

    //广播类型
    public static final String KEY_LOCAL_BROACAST = "intent_is_local_broadcasr";
    public static final String KEY_STICKY_BROACAST = "intent_is_sticky_broadcast";
/**********************************/
    private static ContextReceiver mInstance;

    private ContextReceiver(){

    }

    public static EventReceiver getReceiverInstance() {
        if (mInstance == null) {
            mInstance = new ContextReceiver();
        }
        return mInstance;
    }

    public static EventRegister getRegisterInstance() {
        if (mInstance == null) {
            mInstance = new ContextReceiver();
        }
        return mInstance;
    }

    protected void goActivity(EventBuilder.Event<Bundle, Object> ev){
        Intent intent = getIntent(ev);
        boolean forResult = ev.requestData.getBoolean(KEY_START_FOR_RESULT, false);
        int requestCode = ev.requestData.getInt(KEY_REQUEST_CODE, 200);
        if (ev.reference != null && ev.reference.get() != null
                && ev.reference.get() instanceof  Context) {
            if (forResult) {
                if (ev.reference.get() instanceof Activity) {
                    ((Activity) ev.reference.get()).startActivityForResult(intent, requestCode);
                    ev.responseData = true;
                } else {
                    Log.e(TAG, ">>>>ev.reference.get() cannot Cast to Activity, maybe it is not a" +
                            " Activity,so cannot invoke startActivityForResult method.");
                    ev.responseData = false;
                }
            } else {
                ((Context)ev.reference.get()).startActivity(intent);
                ev.responseData = true;
            }
        } else {
            Log.e(TAG, "ev.reference is null, or ev.reference.get() is null, or is not Context, cannot start activity!");
            ev.responseData = false;
            if (DEBUG) {
                throw new NullPointerException("ev.reference is null or ev.reference.get() is null,cannot start activity!");
            }
        }
        ev.performCallback(ev);

    }

    protected void sendBroadcast(EventBuilder.Event<Bundle, Object> ev){
        Intent intent = getIntent(ev);
        if (ev.reference != null && ev.reference.get() != null) {
            if (ev.requestData.getBoolean(KEY_LOCAL_BROACAST, false)) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance((Context)ev.reference.get());
                lbm.sendBroadcast(intent);
                ev.responseData = true;
            } else {
                Context context = ((Context)ev.reference.get());
                if (!ev.requestData.getBoolean(KEY_STICKY_BROACAST, false)) {
                    context.sendBroadcast(intent);
                    ev.responseData = true;
                } else {
                    context.sendStickyBroadcast(intent);
                    ev.responseData = true;
                }
            }
        } else {
            Log.e(TAG, "ev.reference is null or ev.reference.get() is null,cannot send broadcast!");
            ev.responseData = false;
            if (DEBUG) {
                throw new NullPointerException("ev.reference is null, or ev.reference.get() is null, or is not Context, cannot send broadcast!");
            }
        }
        ev.performCallback(ev);
    }

    protected void startService(EventBuilder.Event<Bundle, Object> ev){
        Intent intent = getIntent(ev);
        if (ev.reference != null && ev.reference.get() != null) {
            ((Context)ev.reference.get()).startService(intent);
            ev.responseData = true;
        } else {
            Log.e(TAG, "ev.reference is null, or ev.reference.get() is null, or is not Context, cannot start service!");
            ev.responseData = false;
            if (DEBUG) {
                throw new NullPointerException("ev.reference is null or ev.reference.get() is null,cannot start service!");
            }
        }
        ev.performCallback(ev);
    }

    protected Intent getIntent(EventBuilder.Event<Bundle, Object> ev) {
        Intent intent = ev.requestData.getParcelable(KEY_INTENT);
        if (intent == null) {
            intent = new Intent();
            intent.putExtra(KEY_BUNDLE, ev.requestData.getBundle(KEY_BUNDLE));
            Serializable serializable = ev.requestData.getSerializable(KEY_CLASS);
            String action = ev.requestData.getString(KEY_ACTION);
            if (serializable != null) {
                intent.setClass((Context) ev.reference.get(), (Class<?>) serializable);
//                ev.requestData.remove(KEY_CLASS);
            } else if (!TextUtils.isEmpty(action)) {
                intent.setAction(action);
//                ev.requestData.remove(KEY_ACTION);
            } else {
                throw new ContextNoActionException("start Context failed,there is no action or class to go!");
            }

            Uri data = ev.requestData.getParcelable(KEY_DATA);
            if (data != null) {
                intent.setData(data);
//                ev.requestData.remove(KEY_DATA);
            }

            String type = ev.requestData.getString(KEY_TYPE);
            if (!TextUtils.isEmpty(type)) {
                intent.setType(type);
//                ev.requestData.remove(KEY_TYPE);
            }

            String category = ev.requestData.getString(KEY_CATEGORY);
            if (!TextUtils.isEmpty(category)) {
                intent.addCategory(category);
//                ev.requestData.remove(KEY_CATEGORY);
            }
        } /*else {
            ev.requestData.remove(KEY_INTENT);
        }*/
        return intent;
    }

    @Override
    public void onReceive(EventBuilder.Event<Bundle, Object> event) {
        switch (event.requestId) {
            case REQUEST_GO_ACTIVITY: {
                goActivity(event);
                break;
            }
            case REQUEST_SEND_BROADCAST: {
                sendBroadcast(event);
                break;
            }
            case REQUEST_START_SERVICE: {
                startService(event);
                break;
            }
        }
    }

    @Override
    public EventReceiver getReceiver(String key) {
        return this;
    }

    protected static class ContextNoActionException extends RuntimeException{

        public ContextNoActionException(String str) {
            super(str);
        }

    }


}
