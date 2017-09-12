 ************************************��Ӧ�¼�����ԭ����ϸ����********************************************
 * ���¼�����ģ�����������Ϊһ�ҿ�ݹ�˾��ÿһ����ݱ������Event�������¼�����ģ����Ҫ����������������*
 * 1���Ǽ�ע����Ʒ��Ӧ�̹�˾������EventRegister��һ��ΪxxxFactory������receiver�Ĺ���(��Ӧ�̵Ŀͻ�);    *
 * 2���������ͳ������Ϳ��(�������͵ĵط�Ҳ����xxxDispatcher)��ÿһ����Ӧ�̹�˾��Ӧһ�����ͳ�( ÿһ���� *
 * ������ͳ�Ҳ����һ��xxxScheduler)�����������͵ķ�ʽ�����ͳ����Բ�ͬ��˾һ����;                     *
 * 3�����ջ����յ����(Ҳ����EventReceiver)                                                             *
 * ��Ҫ��Ա��˵����                                                                                     *
 * Event : ������ɿ�ݵ����                                                                           *
 * EventHandler : ��ݹ�˾��ָ�Ӳ�                                                                      *
 * EventFactory : ��ݹ�˾                                                                              *
 * EventRegister : ��Ӧ�̹�˾(����ʵ��������Eventģ��)��ÿһ�ҹ�Ӧ�̹�˾��Ҫ���ݹ�˾ע��Ǽǣ����Ա���*
 * ʵ�ִ˽ӿڣ���Ӧ�̹�˾����߿�ݹ�˾����(Event)Ҫ�����ĸ��ռ��ˡ�                                    *
 * EventReceiver : �ռ���(����ʵ��������Eventģ��)����Ϊ�ռ��˱���ʵ�ִ˽ӿڣ�����������յ����(Event) *
 *,֮�����ʵ����������(Event)                                                                      *
 ********************************************************************************************************

 github��ַ��https://github.com/haoxiangtt/EventAndroid


һ��ֱ��ʹ�õķ�����
	1������һ��event�����͸���������
	Bundle bundle = new Bundle();
	//bundle.putXXX(....);
	new EventBuilder<Bundle, JsonObject>()
		.receiver(new EventReceiver<Bundle, JsonObject>(){//���ý�����(�ջ���)
			@Override
			public void onReceive(EventBuilder.Event<Bundle, JsonObject> event){
			    if (event.requestId == 0) {
					Log.d("tag","hello,receive the event, requestId = 0");
				} else {
					Log.d("tag","hello,receive the event, requestId = " + event.requestId);
				}
				event.responseData = new JsonObject("{code:'0',message:'hello event android.'}");
				event.performCallback(event);//ע�⣺���Լ���ҵ������֮����Ҫ�����ص��ͱ������һ�´˷���
				
			}
		}).requestId(0)
		.target(EventHandler.getInstance())//���ݾ������Ҫ
		.reference(new WeakReference<Context>(this/*your context*/))//�Ǳ�Ҫ
		.requestBundle(bundle)//request parameter�������б��Ǳ�Ҫ
		.dispatcher(new BaseEventDispatcher())//���÷ַ���, �Ǳ�Ҫ
		.subscribeOn(Schedulers.cache())//���õ���ʱ�����̣߳�Ĭ��Ϊcache�̣߳��Ǳ�Ҫ
		.observerOn(Schedelers.ui())//���ûص�ʱ���ڵ��̣߳�Ĭ��Ϊcache�̣߳��Ǳ�Ҫ
		.callback(new EventCallback<Bundle, JsonObject>(){//���ûص����Ǳ�Ҫ
			@Override
				public  void call(EventBuilder.Event<Bundle, JsonObject> event) {
					//....
					Log.d("hello, handle callback, responseData = " + event.responseData.toString());
				}
		}).build().send();
		
		
	����ͨ����ע�����ͷַ�����ʹ�ã�
	1��������Ҫ�ڵ���event����ǰע��ע����(ʵ����EventRigister�ӿڵ��Զ�����)��
	   ע��������ҪĿ���ǰ������ҵ���Ӧ�Ľ�����(ʵ����EventReceiver�ӿڵ��Զ�����)��
	   һ��ע��ע�����ͽ�����������Application��Activity��onCreate�����У�
	   EventRegister�ӿں�EventReceiver�ӿڵ�ʵ�ֿ��Բο�ContextReceiver���ʵ�֡�
	   
	   //��ҵ��ģ�ͣ���ModelFactory�����Զ����ע������ʵ����EventRegister�ӿ�
        ModelFactory.getInstance().registModelProxy(this, MainModel.class, Constant.MAIN_MODEL/*���ǻ�ȡ��������key*/);

        //�󶨷ַ�����ע����ҵ�����Event��registerType��receiverKey����������Ч.
        //��ҵ��ģ�͹���ע�ᵽ�¼���������
        EventFactory.getEventRegisterFactory().bindDispatcher(
			Constant.EVENT_TYPE_MODEL/*�����Ӧevent�е�registerType������event������registerType�����ͨ��������ҵ���Ӧ��ע����
			��������type�����������ж��壬��ʱevent��д��ʱ���Ӧ�Ϳ�����*/,
			ModelFactory.getRegister()/*���Լ�������*/);
			
		//�������ע�������ɿ���ڲ��ṩ�ģ���Ҫ��������������activity�����������͹㲥����������
        EventFactory.getEventRegisterFactory().bindDispatcher(
		Constant.EVENT_TYPE_CONTEXT,
		ContextReceiver.getRegisterInstance());
		
        //Ϊҵ�񹤳�����ַ�������һ���ǷǱ�Ҫ�ģ�����ѡ�񲻰󶨡�
		//����Ӧtype��ע�����ṩ�ַ�����ע������ĵ�һ������typeҪ��󶨵��Ǹ�ע������Ӧ��
		//��������䣬���ʹ��Ĭ�ϵķַ���������ڵ���ʱ��ʱ�����˷ַ������ʹ����ʱ�ķַ�����
        EventFactory.getEventRegisterFactory().bindDispatcher(Constant.EVENT_TYPE_MODEL, new DefaultEventDispatcher());
        EventFactory.getEventRegisterFactory().bindDispatcher(Constant.EVENT_TYPE_CONTEXT, new ContextEventDispatcher());
		
		������󶨹��������ˣ�������δ��뽨��д��Application���У�ͨ�����ַ�ʽʵ��Event���ƿ��Խ����Լ���Ŀ��ҵ��ģ���
		EventAndroid��ܰ󶨣�������������ô���ã�
		
		Bundle bundle = new Bundle();//�����Լ����������
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
			.type(Constant.EVENT_TYPE_MODEL)//��д��ע����������
			.key(Constant.MAIN_MODEL)//ע����ͨ�����key�ҵ���Ӧ�Ľ��������̳���EventRegister�ӿڵ�ע������ʵ��getReceiver������
			//��������Ĳ���ֻ��һ�����������ﴫ��ȥ��key��
			.requestId(0)
			.startTime(System.currentTimeMillis())
			.target(EventHandler.getInstance())
			.requestBundle(bundle)
			.callback(new EventCallback<Bundle, JsonObject>() {
				@Override
				public  void call(EventBuilder.Event<Bundle, JsonObject> event) {
				
					parseData(event);
					
				}
			}).subscribeOn(Schedulers.cache())
			.observeOn(Schedulers.ui())
			.build();

		event.send();
		
		
		
		
		
		
		
		
		
		