package event.router;

import android.content.Context;
import android.net.Uri;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import event.ContextEventDispatcher;
import event.ContextReceiver;
import event.base.EventFactory;
import event.router.interfaces.EventRelease;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXX
 * author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2018/7/17 0017
 * modifyDate : 2018/7/17 0017
 * @version    : 1.0
 * desc       :
 * </pre>
 */
public class EventRouter {
    public static final int REQUEST_GO_ACTIVITY = ContextReceiver.REQUEST_GO_ACTIVITY;
    public static final int REQUEST_SEND_BROADCAST = ContextReceiver.REQUEST_SEND_BROADCAST;
    public static final int REQUEST_START_SERVICE = ContextReceiver.REQUEST_START_SERVICE;

    public static final int TYPE_CONTEXT_FACTORY = 1095;

    private static EventRouter mInstant;

    private Context mBaseContext;

    private boolean isInitialize = false;

    private EventRouter() {
    }

    public void init(Context context){
        mBaseContext = context.getApplicationContext();
        EventFactory.getEventRegisterFactory()
            .bindRegister(TYPE_CONTEXT_FACTORY, ContextReceiver.getRegisterInstance())
            .bindDispatcher(TYPE_CONTEXT_FACTORY, new ContextEventDispatcher());
        Utils.initializeRouterMapper();
        isInitialize = true;
    }

    public static EventRouter getInstant() {
        if (mInstant == null) {
            mInstant = new EventRouter();
        }
        return mInstant;
    }

    public PostCard build(String path) {
        if (!isInitialize){
            throw new IllegalStateException("Event Router not initialize, cannot build.");
        }
        return new PostCard(mBaseContext, path);
    }

    public PostCard build(Uri uri) {
        if (!isInitialize){
            throw new IllegalStateException("Event Router not initialize, cannot build.");
        }
        return new PostCard(mBaseContext, uri);
    }

    public PostCard build() {
        if (!isInitialize){
            throw new IllegalStateException("Event Router not initialize, cannot build.");
        }
        return new PostCard(mBaseContext);
    }

    public PostCard buildUrl(String url) {
        if (!isInitialize){
            throw new IllegalStateException("Event Router not initialize, cannot build.");
        }
        return new PostCard(url, mBaseContext);
    }

    public EventRelease inject(Object target) {
        if (!isInitialize){
            throw new IllegalStateException("Event Router not initialize, cannot inject.");
        }
        Class<?> targetClass = target.getClass();
        Constructor<? extends EventRelease> constructor = Utils.findInjectorConstructorForClass(targetClass);
        if (constructor == null) {
            return EventRelease.EMPTY;
        }
        try {
            return constructor.newInstance(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Unable to invoke " + constructor, e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new RuntimeException("Unable to create binding instance.", cause);
        }
    }

    public void release(){
        Utils.release();
        mBaseContext = null;
        mInstant = null;
        isInitialize = false;
    }

}
