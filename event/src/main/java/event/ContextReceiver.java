package event;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;

import event.base.EventBuilder;
import event.base.EventConfig;
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
 * @version    : 1.1
 * desc       : android上下文接收器兼注册器；页面跳转、广播发送、服务开启的模型（核心业务模型）
 * </pre>
 */

public final class ContextReceiver implements EventReceiver<Bundle, Object>, EventRegister {

    private static boolean DEBUG = EventConfig.isDebugMode();

    private static final String TAG = "event-android";

    public static final int REQUEST_GO_ACTIVITY = 0;
    public static final int REQUEST_SEND_BROADCAST = 1;
    public static final int REQUEST_START_SERVICE = 2;


    //如果调用startActivityForResult,则此项设置为true
    public static final String KEY_START_FOR_RESULT = "intent_for_result";
    //如果调用如果调用startActivityForResult，需要填写一个requestCode作为请求码
    public static final String KEY_REQUEST_CODE = "intent_request_code";
    public static final int DEFAULT_REQUEST_CODE = 200;

    public static final String KEY_TRANSITION_ANIMATION = "intent_transition_anim";

    //直接从requestBundle中过去用户定义的intent
    public static final String KEY_INTENT = "intent_intent";

/*程序自动生成一个需要发送的intent*/
    //获取所要传递的数据
    public static final String KEY_EXTRAS = "intent_extras";
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
    public static final String KEY_CATEGORIES = "intent_categories";
    //获取需要设置的flag值
    public static final String KEY_FLAGS = "intent_flags";

    //广播类型
    public static final String KEY_BROADCAST_PERMISSION = "intent_broadcast_permission";
    public static final String KEY_BROADCAST_TYPE = "intent_broadcast_type";
    public static final int KEY_COMMON_BROADCAST = 0;
    public static final int KEY_LOCAL_BROADCAST = 1;
    public static final int KEY_STICKY_BROADCAST = 2;
    public static final int KEY_ORDER_BROADCAST = 3;

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
        int requestCode = ev.requestData.getInt(KEY_REQUEST_CODE, DEFAULT_REQUEST_CODE);
        if (ev.reference != null && ev.reference.get() != null) {
            try {
                if (forResult) {
                    if (ev.reference.get() instanceof Activity) {
                        Activity activity = (Activity) ev.reference.get();
                        activity.startActivityForResult(intent, requestCode);
                        int[] anims = ev.requestData.getIntArray(KEY_TRANSITION_ANIMATION);
                        if (anims != null && anims.length == 2) {
                            activity.overridePendingTransition(anims[0], anims[1]);
                        }
                        ev.responseData = true;
                    } else if (ev.reference.get() instanceof Fragment) {
                        Fragment fragment = (Fragment) ev.reference.get();
                        fragment.startActivityForResult(intent, requestCode);
                        int[] anims = ev.requestData.getIntArray(KEY_TRANSITION_ANIMATION);
                        if (anims != null && anims.length == 2) {
                            fragment.getActivity().overridePendingTransition(anims[0], anims[1]);
                        }
                        ev.responseData = true;
                    } else {
                        Log.e(TAG, ">>>>ev.reference.get() cannot Cast to Activity or Fragment, maybe it is not a" +
                                " Activity or Fragment,so cannot invoke startActivityForResult method.");
                        ev.responseData = false;
                    }
                } else {
                    if (ev.reference.get() instanceof Context) {
                        ((Context) ev.reference.get()).startActivity(intent);
                        if (ev.reference.get() instanceof Activity) {
                            int[] anims = ev.requestData.getIntArray(KEY_TRANSITION_ANIMATION);
                            if (anims != null && anims.length == 2) {
                                ((Activity) ev.reference.get()).overridePendingTransition(anims[0], anims[1]);
                            }
                        }
                    } else if (ev.reference.get() instanceof Fragment) {
                        Fragment fragment = (Fragment) ev.reference.get();
                        fragment.startActivity(intent);
                        int[] anims = ev.requestData.getIntArray(KEY_TRANSITION_ANIMATION);
                        if (anims != null && anims.length == 2) {
                            fragment.getActivity().overridePendingTransition(anims[0], anims[1]);
                        }
                    }
                    ev.responseData = true;
                }
            } catch ( ActivityNotFoundException e) {
                Log.e(TAG, e.getMessage());
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    Log.e(TAG, stackTraceElement.toString());
                }
                ev.responseData = false;
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

    /**
     * if use StickyBroadcast request permission {@link android.Manifest.permission#BROADCAST_STICKY}
     * @param ev
     */
    @SuppressLint("MissingPermission")
    protected void sendBroadcast(EventBuilder.Event<Bundle, Object> ev){
        Intent intent = getIntent(ev);
        if (ev.reference != null && ev.reference.get() != null) {
            Context context = null;
            if (ev.reference.get() instanceof Context) {
                context = ((Context)ev.reference.get());
            } else if (ev.reference.get() instanceof Fragment) {
                context = ((Fragment)ev.reference.get()).getActivity();
            } else {
                Log.e(TAG, ">>>>ev.reference.get() cannot Cast to Context, maybe it is not" +
                        " Context type,so cannot send broadcast.");
                return;
            }
            int startType = ev.requestData.getInt(KEY_BROADCAST_TYPE, KEY_COMMON_BROADCAST);
            String permission = ev.requestData.getString(KEY_BROADCAST_PERMISSION);
            if ( startType== KEY_LOCAL_BROADCAST) {
                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
                lbm.sendBroadcast(intent);
                ev.responseData = true;
            } else if(startType == KEY_STICKY_BROADCAST) {
                context.sendStickyBroadcast(intent);
                ev.responseData = true;
            } else if (startType == KEY_ORDER_BROADCAST) {
                if (TextUtils.isEmpty(permission)) {
                    context.sendBroadcast(intent);
                } else {
                    context.sendOrderedBroadcast(intent, permission);
                }
            } else {
                if (TextUtils.isEmpty(permission)) {
                    context.sendBroadcast(intent);
                } else {
                    context.sendBroadcast(intent, permission);
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
            Context context = null;
            if (ev.reference.get() instanceof Context) {
                context = ((Context)ev.reference.get());
            } else if (ev.reference.get() instanceof Fragment) {
                context = ((Fragment)ev.reference.get()).getActivity();
            }
            context.startService(intent);
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
            intent.putExtras(ev.requestData.getBundle(KEY_EXTRAS));
            Serializable serializable = ev.requestData.getSerializable(KEY_CLASS);
            String action = ev.requestData.getString(KEY_ACTION);
            if (serializable != null
                    && ev.reference.get() != null
                    && (ev.reference.get() instanceof Context || ev.reference.get() instanceof Fragment)) {
                if (ev.reference.get() instanceof Context) {
                    intent.setClass((Context)ev.reference.get(), (Class<?>) serializable);
                } else if (ev.reference.get() instanceof Fragment) {
                    intent.setClass(((Fragment)ev.reference.get()).getActivity(), (Class<?>) serializable);
                }
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
            String[] categorys = ev.requestData.getStringArray(KEY_CATEGORIES);
            if (categorys != null && categorys.length > 0) {
                for (String s : categorys) {
                    intent.addCategory(s);
                }
            }
            int flags = ev.requestData.getInt(KEY_FLAGS);
            intent.addFlags(flags);
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
            default:{
                Log.e(TAG, "not found the requestId, please set requestId to event object, \n" +
                    "{@link #REQUEST_START_SERVICE}、{@link #REQUEST_SEND_BROADCAST}、{@link #REQUEST_GO_ACTIVITY}");
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
