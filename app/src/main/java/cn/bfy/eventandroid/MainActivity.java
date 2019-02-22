package cn.bfy.eventandroid;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.lang.ref.SoftReference;

import event.ContextEventDispatcher;
import event.ContextReceiver;
import event.DefaultEventDispatcher;
import event.base.EventBuilder;
import event.base.EventCallback;
import event.base.EventFactory;
import event.base.EventHandler;
import event.base.EventReceiver;
import event.base.EventRegister;
import event.base.Interceptor;
import event.base.Schedulers;
import event.router.EventRouter;
import event.router.annotation.Autowired;
import event.router.annotation.Router;
import event.router.interfaces.EventRelease;

@Router(path = "/test/main", type = Router.Type.COMPONENT_ACTIVITY)
public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    Handler mHandler;

    @Autowired(path = "/test/model1", singleton = true)
    Model1 model1;

    Model1 model11;

    @Autowired
    Model2 model2;

    Model2 model22;


    private EventRelease eventRelease;

    private int count = 0;

    @Autowired(singleton = true)
    protected void setModel1(Model1 model) {
        model11 = model;
    }

    @Autowired(path = "/test/model2")
    public void setModel2(Model2 model) {
        model22 = model;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
        //使用EventFactory将自己自定义的注册器绑定到EventAndroid框架中，这里我们用框架提供的自定义注册器来举例
        //ContextReceiver是一个专门处理界面跳转，发送广播和开启服务的注册器，通过注册器获取到的接收器只有一个
        EventFactory.getEventRegisterFactory()
            .bindRegister(0/*注册器的唯一标识，可自定义*/, ContextReceiver.getRegisterInstance())
            .bindDispatcher(0/*注册器的唯一标识，告诉框架为哪个注册器注册分发器*/
                , new ContextEventDispatcher());
        EventRouter.getInstant().init(this);

        setContentView(R.layout.activity_main);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);
        findViewById(R.id.btn3).setOnClickListener(this);
        findViewById(R.id.btn4).setOnClickListener(this);
        findViewById(R.id.btn5).setOnClickListener(this);

        eventRelease = EventRouter.getInstant().inject(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn1) {
            goActivity();
        } else if (id == R.id.btn2) {
            handleTask();
        } else if (id == R.id.btn3) {
            route();
        } else if (id == R.id.btn4) {
            goService();
        } else if (id == R.id.btn5) {
            showModel();
        }
    }

    private void showModel() {
        switch (count){
            case 0: {
                model1.show(this, "我是1号模型");
                break;
            }
            case 1: {
                model11.show(this, "我是11号模型");
                break;
            }
            case 2: {
                model2.show(this, "我是2号模型");
                break;
            }
            case 3: {
                model22.show(this, "我是22号模型");
                break;
            }
        }
        count = (count+1) % 4;
    }

    private void goService() {
        EventRouter.getInstant().build("/test/myservice")
            .withCallback(new EventCallback<Bundle, Boolean>() {
                @Override
                public void call(EventBuilder.Event<Bundle, Boolean> event) {
                    if (event.responseData) {
                        Toast.makeText(MainActivity.this,
                            "router callback.",Toast.LENGTH_SHORT).show();
                    }

                }
            }).letsGo();
    }

    /**
     * 页面跳转新接口
     * 路由接口高仿阿里的Arouter设计
     */
    private void route() {
        EventRouter.getInstant().build("/test/hello").
            withRequestCode(5002)
            .withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .withContext(this)
            .letsGo();
    }

    /**
     * 扩展了event-android的路由框架，这种跳转页面的方式就过时了！
     */
    private void goActivity() {
        Bundle request = new Bundle();
        request.putBoolean(ContextReceiver.KEY_START_FOR_RESULT, true);//设置是否回调
        request.putInt(ContextReceiver.KEY_REQUEST_CODE, 5002);//设置回调的rquestCode
        Intent intent = new Intent(this, HelloActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        request.putParcelable(ContextReceiver.KEY_INTENT, intent);//设置跳转的意图实例
        new EventBuilder<Bundle, Object>()
            .type(0)//设置注册器的唯一标识，就是在Eventfactry中注册的那个type
            .key("")//注册器通过这个key来找到对应的接收器，因为ContextReceiver注册器只有一个接收器，这里可以不配置
            .requestId(ContextReceiver.REQUEST_GO_ACTIVITY)//设置请求id，接收器通过这个id判断执行哪个方法
            .requestData(request)//设置请求参数列表, 非必要
            .sessionId("")//设置会话id, 非必要
            .startTime(System.currentTimeMillis())//设置发送的开始时间, 非必要
            .reference(new SoftReference(this))//设置一个引用，供接收器里面调用，ContextReceiver需要一个Context容器, 非必要
            .target(EventHandler.getInstance())//必要
            .subscribeOn(Schedulers.ui())//非必要
            .build().send();//构建event并发送
    }

    /**
     * 使用这种方式不用EventFactory去绑定注册器和分发器
     */
    private void handleTask() {
        EventBuilder.Event<Bundle, Object> event = new EventBuilder<Bundle, Object>()
            .register(new EventRegister() {//实例化一个注册器来构建自己的Receiver接收器
                @Override
                public EventReceiver getReceiver(String key) {
                    return null;//这里我们不用接收器，所以返回null，如果用户返回自己的接收器就不用下面的方法构建接收器了
                }
            }).receiver(new EventReceiver<Bundle, Object>() {//构建一个接收器，
                //如果用此方法构建了一个接收器，就可以不用注册器去构建接收器了，所以上面可以返回null
                @Override
                public void onReceive(EventBuilder.Event<Bundle, Object> event) {
                    Toast.makeText(MainActivity.this, "event android正在执行任务"
                            , Toast.LENGTH_SHORT).show();
                    event.responseData = "响应信息";
                    event.performCallback(event);//一定要调用这一句话，才能触发后面的回调
                }
            }).dispatcher(new DefaultEventDispatcher()//构建分发器，使用默认的。非必要
            ).interceptor(new Interceptor<Bundle, Object>() {//使用拦截器, 非必要
                @Override
                public boolean intercept(EventState state, EventBuilder.Event<Bundle, Object> event) {
                    return false;//返回true，任务会被拦截，中断后续操作，这里不使用拦截
                }
            }).subscribeOn(/*Schedulers.cache()*/Schedulers.ui()//构建接收器中执行的任务所在的调度器，
                    // 框架为我们设计了两个调度器，一个是cache，一个是ui, 默认是cache线程，非必要
            ).observeOn(Schedulers.ui()//构建回调（观察者）所在的调度器, 默认是cache线程，非必要
            ).delay(0, null)//任务延时发送, 非必要
            .target(EventHandler.getInstance()//Event控制器，操作句柄。必要
            ).callback(new EventCallback<Bundle, Object>() {//回调, 非必要
                @Override
                public void call(EventBuilder.Event<Bundle, Object> event) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "event android正在执行回调"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    }, 2000);
                    String response = event.responseData.toString();//可以从event中获取响应信息
                    event.release();//必要的时候可以在使用完event对象时释放对象，避免内存泄露
                }
            }).build();
        event.send();//发送

    }

    @Override
    protected void onDestroy() {
        eventRelease.release();
        EventRouter.getInstant().release();
        super.onDestroy();
    }
}
