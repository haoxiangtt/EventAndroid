package event.router;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.AnimRes;
import android.support.v4.app.Fragment;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

import event.ContextReceiver;
import event.base.EventBuilder;
import event.base.EventCallback;
import event.base.EventHandler;
import event.base.Schedulers;
import event.base.Subscription;
import event.router.annotation.Router;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2018/6/20 0020
 * modifyDate : 2018/6/20 0020
 * @version    : 1.0
 * desc       :
 * </pre>
 */
public class PostCard {


    public static final int REQUEST_GO_ACTIVITY = ContextReceiver.REQUEST_GO_ACTIVITY;
    public static final int REQUEST_SEND_BROADCAST = ContextReceiver.REQUEST_SEND_BROADCAST;
    public static final int REQUEST_START_SERVICE = ContextReceiver.REQUEST_START_SERVICE;

    //如果调用startActivityForResult,则此项设置为true
    public static final String KEY_START_FOR_RESULT = ContextReceiver.KEY_START_FOR_RESULT;
    //如果调用如果调用startActivityForResult，需要填写一个requestCode作为请求码
    public static final String KEY_REQUEST_CODE = ContextReceiver.KEY_REQUEST_CODE;

    public static final String KEY_TRANSITION_ANIMATION = ContextReceiver.KEY_TRANSITION_ANIMATION;

    //广播类型
    public static final String KEY_BROADCAST_PERMISSION = ContextReceiver.KEY_BROADCAST_PERMISSION;
    public static final String KEY_BROADCAST_TYPE = ContextReceiver.KEY_BROADCAST_TYPE;
    public static final int KEY_COMMON_BROADCAST = ContextReceiver.KEY_COMMON_BROADCAST;
    public static final int KEY_LOCAL_BROADCAST = ContextReceiver.KEY_LOCAL_BROADCAST;
    public static final int KEY_STICKY_BROADCAST = ContextReceiver.KEY_STICKY_BROADCAST;
    public static final int KEY_ORDER_BROADCAST = ContextReceiver.KEY_ORDER_BROADCAST;

    private EventBuilder<Bundle, Boolean> builder;
    private Bundle requestBundle = null;
    private Intent intent;

    private boolean lockRequestId = false;

    PostCard(Context context) {
        builder = new EventBuilder<>();
        requestBundle = new Bundle();
        intent = new Intent();
        requestBundle.putParcelable(ContextReceiver.KEY_INTENT, intent);
        builder.requestData(requestBundle)
            .type(EventRouter.TYPE_CONTEXT_FACTORY)
            .reference(new WeakReference(context))
            .subscribeOn(Schedulers.ui())
            .observeOn(Schedulers.ui())
            .target(EventHandler.getInstance());
    }

    PostCard(Context context, String path) {
        builder = new EventBuilder<>();
        requestBundle = new Bundle();
        Map<String, Object> infoMap = Utils.findRouteInfoFromPath(path);
        if (infoMap != null) {
            Class<?> cls = (Class<?>) infoMap.get("path");
            int type = Integer.valueOf(infoMap.get("type").toString()).intValue();
            if (type == Router.Type.COMPONENT_ACTIVITY) {
                builder.requestId(REQUEST_GO_ACTIVITY);
            } else if (type == Router.Type.COMPONENT_SERVICE) {
                builder.requestId(REQUEST_START_SERVICE);
            } else {
                throw new IllegalArgumentException("the router component(path:" + path
                    + ") is not a android component, cannot route.");
            }
            intent = new Intent(context, cls);
            lockRequestId = true;
        } else {
            intent = new Intent();
        }
        requestBundle.putParcelable(ContextReceiver.KEY_INTENT, intent);
        builder.requestData(requestBundle)
            .type(EventRouter.TYPE_CONTEXT_FACTORY)
            .reference(new WeakReference(context))
            .subscribeOn(Schedulers.ui())
            .observeOn(Schedulers.ui())
            .target(EventHandler.getInstance());
    }

    PostCard(Context context, Uri data) {
        builder = new EventBuilder<>();
        requestBundle = new Bundle();
        intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(data);
        requestBundle.putParcelable(ContextReceiver.KEY_INTENT, intent);
        builder.requestData(requestBundle)
            .type(EventRouter.TYPE_CONTEXT_FACTORY)
            .requestId(REQUEST_GO_ACTIVITY)
            .reference(new WeakReference(context))
            .subscribeOn(Schedulers.ui())
            .observeOn(Schedulers.ui())
            .target(EventHandler.getInstance());
        lockRequestId = true;
    }

    public PostCard(String url, Context context) {
        try {
            intent = Intent.parseUri(url,  Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                intent.setSelector(null);
            }
            requestBundle = new Bundle();
            requestBundle.putParcelable(ContextReceiver.KEY_INTENT, intent);
            builder.requestData(requestBundle)
                    .type(EventRouter.TYPE_CONTEXT_FACTORY)
                    .requestId(REQUEST_GO_ACTIVITY)
                    .reference(new WeakReference(context))
                    .subscribeOn(Schedulers.ui())
                    .observeOn(Schedulers.ui())
                    .target(EventHandler.getInstance());
            lockRequestId = true;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }

    public PostCard withContext(Context context) {
        builder.reference(new WeakReference(context));
        return this;
    }

    public PostCard withContext(Fragment fragment) {
        builder.reference(new WeakReference(fragment));
        return this;
    }

    /**
     *
     * @param requestId see
     * {@link EventRouter#REQUEST_GO_ACTIVITY}
     * {@link EventRouter#REQUEST_SEND_BROADCAST}
     * {@link EventRouter#REQUEST_GO_ACTIVITY}
     * @return xxx
     */
    public PostCard withWhere(int requestId){
        if (!lockRequestId) {
            builder.requestId(requestId);
        }
        return this;
    }


    public PostCard withTransition(@AnimRes int enterAnim, @AnimRes int exitAnim) {
        requestBundle.putIntArray(KEY_TRANSITION_ANIMATION, new int[]{enterAnim, exitAnim});
        return this;
    }

    public PostCard withRequestCode(int requestCode) {
        requestBundle.putBoolean(KEY_START_FOR_RESULT, true);
        requestBundle.putInt(KEY_REQUEST_CODE, requestCode);
        return this;
    }


    public PostCard withComponent(ComponentName component) {
        intent.setComponent(component);
        return this;
    }

    public PostCard withAction(String action) {
        intent.setAction(action);
        return this;
    }

    public PostCard withData(Uri data) {
        intent.setData(data);
        return this;
    }

    public PostCard withType(String type) {
        intent.setType(type);
        return this;
    }

    public PostCard withDataAndType(Uri data, String type){
        intent.setDataAndType(data, type);
        return this;
    }

    public PostCard withFlags(int flags) {
        intent.addFlags(flags);
        return this;
    }

    public PostCard withCategoreis(String... categoreis){
        for (String category : categoreis) {
            intent.addCategory(category);
        }
        return this;
    }

    public PostCard withBroadcastType(int broadcastType) {
        requestBundle.putInt(KEY_BROADCAST_TYPE, broadcastType);
        return this;
    }

    public PostCard withBroadcastPermission(String permission) {
        requestBundle.putString(KEY_BROADCAST_PERMISSION, permission);
        return this;
    }

    public PostCard withCallback(EventCallback<Bundle, Boolean> callback) {
        builder.callback(callback);
        return this;
    }

    public PostCard withExtras(Bundle extras) {
        intent.putExtras(extras);
        return this;
    }

    public PostCard withExtra(String key, short value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, short[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, int value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, int[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, long value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, long[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, float value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, float[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, double value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, double[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, char value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, char[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, byte value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, byte[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, Serializable value) {
        intent.putExtra(key, value);
        return this;
    }


    public PostCard withExtra(String key, Parcelable value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, Parcelable[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, String value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, String[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, CharSequence value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, CharSequence[] value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withExtra(String key, Bundle value) {
        intent.putExtra(key, value);
        return this;
    }

    public PostCard withIntegerArrayListExtra(String key, ArrayList<Integer> value) {
        intent.putIntegerArrayListExtra(key, value);
        return this;
    }

    public PostCard withParcelableArrayListExtra(String key, ArrayList<Parcelable> value) {
        intent.putParcelableArrayListExtra(key, value);
        return this;
    }

    public PostCard withCharSequenceArrayListExtra(String key, ArrayList<CharSequence> value) {
        intent.putCharSequenceArrayListExtra(key, value);
        return this;
    }

    public PostCard withStringArrayListExtra(String key, ArrayList<String> value) {
        intent.putStringArrayListExtra(key, value);
        return this;
    }

    /**
     *route to navigation
     * @return xxx
     */
    public Subscription letsGo() {
        return builder.build().send();
    }

}
