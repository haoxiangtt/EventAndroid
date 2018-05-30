 ************************************响应事件机制原理详细讲解********************************************
 * 此事件机制模块形象地描述为一家快递公司，每一件快递被打包成Event，我们事件机制模块主要做的事情有三件，*
 * 1、登记注册物品供应商公司（就是EventRegister，一般为xxxFactory：生成receiver的工厂(供应商的客户);    *
 * 2、分配派送车俩运送快递(分配派送的地方也就是xxxDispatcher)，每一个供应商公司对应一辆派送车( 每一辆具 *
 * 体的派送车也就是一个xxxScheduler)，但分配派送的方式和派送车可以不同公司一起共享;                     *
 * 3、让收货人收到快递(也就是EventReceiver)                                                             *
 * 主要成员类说明：                                                                                     *
 * Event : 被打包成快递的物件                                                                           *
 * EventHandler : 快递公司的指挥部                                                                      *
 * EventFactory : 快递公司                                                                              *
 * EventRegister : 供应商公司(具体实例不属于Event模块)，每一家供应商公司都要向快递公司注册登记，所以必须*
 * 实现此接口，供应商公司会告诉快递公司货物(Event)要发给哪个收件人。                                    *
 * EventReceiver : 收件人(具体实例不属于Event模块)，作为收件人必须实现此接口，在这里可以收到快递(Event) *
 *,之后根据实际需求处理快递(Event)                                                                      *
 ********************************************************************************************************

 github地址：https://github.com/haoxiangtt/EventAndroid

 引入方式：compile 'com.bfy:event-android:1.0.3'

 混淆规则：无

一、直接使用的方法：
	1、创建一个event并发送给接收器：
	Bundle bundle = new Bundle();
	//bundle.putXXX(....);
	new EventBuilder<Bundle, JsonObject>()
		.receiver(new EventReceiver<Bundle, JsonObject>(){//设置接收器(收货人)
			@Override
			public void onReceive(EventBuilder.Event<Bundle, JsonObject> event){
			    if (event.requestId == 0) {
					Log.d("tag","hello,receive the event, requestId = 0");
				} else {
					Log.d("tag","hello,receive the event, requestId = " + event.requestId);
				}
				event.responseData = new JsonObject("{code:'0',message:'hello event android.'}");
				event.performCallback(event);//注意：在自己的业务处理完之后想要触发回调就必须调用一下此方法
				
			}
		}).requestId(0)
		.target(EventHandler.getInstance())//操纵句柄，必要
		.reference(new WeakReference<Context>(this/*your context*/))//非必要
		.requestBundle(bundle)//request parameter，参数列表，非必要
		.dispatcher(new BaseEventDispatcher())//设置分发器, 非必要
		.subscribeOn(Schedulers.cache())//设置调度时所在线程，默认为cache线程，非必要
		.observerOn(Schedelers.ui())//设置回调时所在的线程，默认为cache线程，非必要
		.callback(new EventCallback<Bundle, JsonObject>(){//设置回调，非必要
			@Override
				public  void call(EventBuilder.Event<Bundle, JsonObject> event) {
					//....
					Log.d("hello, handle callback, responseData = " + event.responseData.toString());
				}
		}).build().send();
		
		
	二、通过绑定注册器和分发器来使用：
	1、我们需要在调用event发送前注册注册器(实现了EventRigister接口的自定义类)；
	   注册器的主要目的是帮我们找到对应的接收器(实现了EventReceiver接口的自定义类)；
	   一般注册注册器和接收器都是在Application或Activity的onCreate方法中；
	   EventRegister接口和EventReceiver接口的实现可以参考ContextReceiver类的实现。
	   
	   //绑定业务模型，类ModelFactory是我自定义的注册器，实现了EventRegister接口
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
		
		到这里绑定工作做完了，上面这段代码建议写在Application类中，通过这种方式实现Event机制可以将你自己项目的业务模块和
		EventAndroid框架绑定；下面来讲下怎么调用：
		
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
		
		
		
		
		
		
		
		
		
		