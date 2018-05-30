package event.base;

import android.text.TextUtils;
import android.util.Log;

import java.util.UUID;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2017/6/20 0020
 * modifyDate : 2017/6/20 0020
 * @version    : 2.0
 * desc       : 分发器基类，做了一些分发器必要的工作。
 * </pre>
 */

public class BaseEventDispatcher implements EventDispatcher {

    /*protected void onError(EventBuilder.Event event, int errorType) {
        LogUtils.e(TAG, "event dispatch error, errorType(" + errorType + ").");
    }*/

    protected Subscription onSchedule(final EventBuilder.Event event) {

        if (event.callback != null) {
            EventCallback wrapCallback = wrapCallback(event.callback);
            if (wrapCallback != null) {
                event.callback = wrapCallback;
            }
        }

        return performSchedule(event);
    }

    protected Subscription performSchedule(EventBuilder.Event event) {

        Scheduler.Worker worker = createWorker(event);
        if (event.getDelay() <= 0) {
            return worker.schedule();
        } else {
            return worker.schedule(event.getDelay(), event.getUnit());
        }
    }

    protected Scheduler.Worker createWorker(EventBuilder.Event event) {
        Scheduler subscriber = event.getSubscriber();
        if (subscriber == null) {
            subscriber = Schedulers.defaultScheduler();
        }
       return subscriber.createWorker(event, new DefaultWorkRunnable(event));
    }

    protected EventCallback wrapCallback(EventCallback callback) {
        return new WrapEventCallback(callback);
    }

    @Override
    public Subscription dispatch(EventBuilder.Event event) {

        /*if (event.getSubscriber() == null) {
            event.setSubscriber(Schedulers.cache());
        }
        if (event.getObserver() == null) {
            event.setObserver(Schedulers.ui());
        }*/
        if (TextUtils.isEmpty(event.sessionId)) {
            String uuid = UUID.randomUUID().toString();
            event.sessionId = uuid.replaceAll("-", "");
        }
        return onSchedule(event);
    }

    protected static class DefaultWorkRunnable implements Runnable {

        private EventBuilder.Event mEvent;

        public DefaultWorkRunnable(EventBuilder.Event event) {
            mEvent = event;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void run() {
            if (mEvent.getReceiver() != null) {
                mEvent.getReceiver().onReceive(mEvent);
                return;
            }
            EventRegister register;
            if (mEvent.getRegister() != null) {
                register = mEvent.getRegister();
            } else {
                register = EventFactory.getEventRegisterFactory()
                        .getRegister(mEvent.registerType);
            }
            if (register == null) {
                Log.e(TAG, "event scheduler error : register is null, register registerType = " + mEvent.registerType + ".");
                return;
            }
            final EventReceiver receiver = register.getReceiver(mEvent.receiverKey);
            if ( receiver == null) {
                Log.e(TAG, "event scheduler error : receiver is null, receiver receiverKey = '" + mEvent.receiverKey + "'.");
                return;
            }
            if (mEvent.getInterceptor() != null
                    && mEvent.getInterceptor().intercept(Interceptor.EventState.BEGIN_WORKING, mEvent)) {
                return;
            }
            try {
                receiver.onReceive(mEvent);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
                for (StackTraceElement item : e.getStackTrace()) {
                    Log.e(TAG, item.toString());
                }
                throw e;
            }

            if (mEvent.getCallback() == null) {
                if (mEvent.getInterceptor() != null) {
                    mEvent.getInterceptor().intercept(Interceptor.EventState.END_WORKING, mEvent);
                }
            }

        }
    }

    protected static class WrapEventCallback implements EventCallback{

        private EventCallback mCallback;

        public WrapEventCallback (EventCallback callback) {
            mCallback = callback;
        }

        public EventCallback getInnerCallback(){
            return mCallback;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void call(EventBuilder.Event event) {
            final EventBuilder.Event ev = event;
            Scheduler observer = event.getObserver();
            if (observer == null) {
                observer = Schedulers.defaultScheduler();
            }
            Scheduler.Worker worker = observer.createWorker(event, new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        if (!ev.isUnsubscribe()) {
                            if (ev.getInterceptor() != null
                                    && ev.getInterceptor().intercept(Interceptor.EventState.CALLBACK, ev)) {
                                return;
                            }
                            mCallback.call(ev);
                        }
                        if (ev.getInterceptor() != null) {
                            ev.getInterceptor().intercept(Interceptor.EventState.END_WORKING, ev);
                        }
                    }
                }
            });
            worker.schedule();
        }
    }



}
