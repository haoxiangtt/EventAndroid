## 一、功能介绍
##### 1、基于观察者模式的响应式编程
##### 2、页面路由功能
##### 3、依赖注入功能

## 二、核心类和接口解释
*     此事件机制模块形象地描述为一家快递公司，每一件快递被打包成Event，我们事件机制模块主要做的事情有三件，
	*     1、登记注册物品供应商公司（就是EventRegister，一般为xxxFactory：生成receiver的工厂(供应商的客户);    
	*     2、分配派送车俩运送快递(分配派送的地方也就是xxxDispatcher)，每一个供应商公司对应一辆派送车( 每一辆具体的派送车
			也就是一个xxxScheduler)，但分配派送的方式和派送车可以不同公司一起共享;                     
	*     3、让收货人收到快递(也就是EventReceiver)                                                             
*     主要成员类说明：                                                                                     
	*     Event : 被打包成快递的物件                                                                           
	*     EventHandler : 快递公司的指挥部                                                                     
	*     EventFactory : 快递公司                                                                              
	*     EventRegister : 供应商公司(具体实例不属于Event模块)，每一家供应商公司都要向快递公司注册登记，所以必须
     	 	实现此接口，供应商公司会告诉快递公司货物(Event)要发给哪个收件人。                                    
	*     EventReceiver : 收件人(具体实例不属于Event模块)，作为收件人必须实现此接口，在这里可以收到快递(Event)之后
			根据实际需求处理快递(Event)
<br>
<br>

## 三、响应式编程模块使用方法：
    引入方式：compile 'com.bfy:event-android:1.0.3'
    混淆规则：无

###    1、直接使用
###### 直接创建EventReceiver并发送：
```Java
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
```

###    2、通过绑定注册器和分发器来使用：
###### 我们需要在调用event发送前注册注册器(实现了EventRigister接口的自定义类)；
###### 注册器的主要目的是帮我们找到对应的接收器(实现了EventReceiver接口的自定义类)；
###### 一般注册注册器和接收器都是在Application或Activity的onCreate方法中；
###### EventRegister接口和EventReceiver接口的实现可以参考ContextReceiver类的实现。

```Java
//绑定业务模型，类ModelFactory是我自定义的注册器，是实现了EventRegister接口的业务模型工厂类
ModelFactory.getInstance().registModelProxy(this, MainModel.class, Constant.MAIN_MODEL/*这是获取接收器的key*/);

//绑定分发器和注册者业务类后，Event的registerType和receiverKey参数才能生效.
//将业务模型工厂注册到事件处理工厂中
EventFactory.getEventRegisterFactory().bindRegister(
	Constant.EVENT_TYPE_MODEL/*这个对应event中的registerType参数，event设置了registerType后就是通过这个查找到对应的注册器
	，在这里type参数可以自行定义，到时event填写的时候对应就可以了*/,
	ModelFactory.getRegister()/*把自己返回来*/);
			
//下面这个注册器是由框架内部提供的，主要功能是用来处理activity的启动、发送广播和启动服务。
EventFactory.getEventRegisterFactory().bindRegister(
	Constant.EVENT_TYPE_CONTEXT,
	ContextReceiver.getRegisterInstance());
		
//为业务工厂分配分发器，这一步是非必要的，可以选择不绑定。
//给对应type的注册器提供分发器；注意这里的第一个参数type要与绑定的那个注册器对应。
//如果不分配，则会使用默认的分发器，如果在调用时临时配置了分发器则会使用临时的分发器。
//分发器可以根据情况自行扩展，本框架提供了两种默认的分发器。
EventFactory.getEventRegisterFactory().bindDispatcher(Constant.EVENT_TYPE_MODEL, new DefaultEventDispatcher());
EventFactory.getEventRegisterFactory().bindDispatcher(Constant.EVENT_TYPE_CONTEXT, new ContextEventDispatcher());
```

    到这里绑定工作做完了，上面这段代码建议写在Application类中，通过这种方式实现Event机制可以将你自己项目的
    业务模块和EventAndroid框架绑定；下面来讲下怎么调用：

```Java
Bundle bundle = new Bundle();//设置自己的请求参数
bundle.putString("keyword", key);
bundle.putString("page", "1");
bundle.putString("pagesize", "30");
bundle.putString("userid", "-1");
bundle.putString("clientver", "");
bundle.putString("platform", "WebFilter");
bundle.putString("tag", "em");
bundle.putString("filter", "2");
bundle.putString("iscorrection", "1");
bundle.putString("privilege_filter", "0");
EventBuilder.Event<Bundle, JsonObject> event = new EventBuilder<Bundle, JsonObject>()
	.type(Constant.EVENT_TYPE_MODEL)//填写好注册器的类型
	.key(Constant.MAIN_MODEL)//注册器通过这个key找到对应的接收器，继承了EventRegister接口的注册器会实现getReceiver方法，
	//这个方法的参数只有一个，就是这里传过去的key。
	.requestId(0)
	.startTime(System.currentTimeMillis())
	.target(EventHandler.getInstance())
	.requestData(bundle)
	.callback(new EventCallback<Bundle, JsonObject>() {
		@Override
		public  void call(EventBuilder.Event<Bundle, JsonObject> event) {
				
			parseData(event);
					
		}
	}).subscribeOn(Schedulers.cache())
	.observeOn(Schedulers.ui())
	.build();
event.send();
```

## 四、页面路由功能的使用：
###### 页面路由功能模块依赖响应式编程模块，其他用法和阿里的ARouter使用方法基本一样，不过阉割了一些功能。
### 1、添加依赖和配置
```Gradle
dependencies {
	compile 'com.bfy:event-android:1.0.4'
	compile 'com.bfy:event-router:1.0.4'
	annotationProcessor 'com.bfy:event-router-compiler:1.0.4'
}
```
### 2、添加注解

###### 添加页面路由注解
```Java
@Router(path = "/test/hello", type = Router.Type.COMPONENT_ACTIVITY)
public class HelloActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);
    }
}
```
###### 添加业务模型路由注解
```Java
@Router(path = "/test/model1", type = Router.Type.COMPONENT_MODEL)
public class Model1 {
    public void show(Context context, String msg) {
        Toast.makeText(context, msg + "->我的hashCode=" + hashCode(), Toast.LENGTH_SHORT).show();
    }
}
```

### 3、初始化路由SDK
###### 在application或者Activity的Oncreate方法里都可以。

```Java
@Override
protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	//...
	//初始化路由SDK
	EventRouter.getInstant().init(this);
	//...
}
```

### 4、发起路由

```Java
/**
* 页面跳转新接口
*/
private void route() {
EventRouter.getInstant().build("/test/hello").
    withRequestCode(5002)
    .withFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    .withContext(this)
    .letsGo();
}
```

### 4、添加依赖注入解耦业务需求

```Java
@Router(path = "/test/main", type = Router.Type.COMPONENT_ACTIVITY)
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
	//path表示需要注入对象的路由路径，singleton表示是否使用单例模式
	//如果不配置path，表示使用属性对应的类型进行注入
	@Autowired(path = "/test/model1", singleton = true)
	Model1 model1;//需要注入的业务模型

	Model1 model11;//需要注入的业务模型

	@Autowired
	Model2 model2;//需要注入的业务模型

	Model2 model22;//需要注入的业务模型
	
	//使用设置器注入
	@Autowired(singleton = true)
	//singleton表示是否使用单例
	//如果不配置path，表示使用传入参数对应的类型进行注入
	protected void setModel1(Model1 model) {
	model11 = model;
	}

	//使用设置器注入
	@Autowired(path = "/test/model2")
	public void setModel2(Model2 model) {
	model22 = model;
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//...
		//初始化路由SDK
		EventRouter.getInstant().init(this);

		//添加依赖注入
		eventRelease = EventRouter.getInstant().inject(this);
		//...
	}
}
```

## 五、其他

### 1、日志开关配置

```Java
EventConfig.setDebugMode(true);
```
      
      
      
      


