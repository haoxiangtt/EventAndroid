
һ��ֱ��ʹ�õķ�����
	1������һ��event�����͸���������
	Bundle bundle = new Bundle();
	//bundle.putXXX(....);
	new EventBuilder()
		.receiver(new EventReceiver(){//���ý�����
			@Override
			public void onReceive(EventBuilder.Event event){
			    if (event.requestId == 0) {
					Log.d("tag","hello,receive the event, requestId = 0");
				} else {
					Log.d("tag","hello,receive the event, requestId = " + event.requestId);
				}
				event.responseData = new JsonObject("{code:'0',message:'hello event android.'}");
				if (event.callback != null) {
					event.callback.call(event);
				}
				
			}
		}).requestId(0)
		.target(EventHandler.getInstance())
		.reference(new WeakReference<Context>(this/*your context*/))
		.requestBundle(bundle)//request parameter
		.dispatcher(new BaseEventDispatcher())//���÷ַ���
		.subscribeOn(Schedulers.cache())//���õ���ʱ�����߳�
		.observerOn(Schedelers.ui())//���ûص�ʱ���ڵ��߳�
		.callback(new EventCallback<JsonObject>(){//���ûص�
			@Override
				public  void call(EventBuilder.Event<JsonObject> event) {
					//....
					Log.d("hello, handle callback, responseData = " + event.responseData.toString());
				}
		}).build().send();
		
		
	����ͨ��ע��ע�����ͷַ�����ʹ�ã�
	1��������Ҫ�ڵ���event����ǰע��ע����(EventRigister)
	   ע��������ҪĿ���ǰ������ҵ���Ӧ�Ľ�����(EventReceiver)��
	   
	   //ע��ҵ��ģ�ͣ���������Զ����ע������ʵ����EventRegister�ӿ�
        ModelFactory.getInstance().registModelProxy(this, MainModel.class, Constant.MAIN_MODEL/*���ǻ�ȡ��������key*/);

        //ע��ַ�����ע����ҵ�����Event��registerType��receiverKey����������Ч.
        //��ҵ��ģ�͹���ע�ᵽ�¼���������
        EventFactory.getEventRegisterFactory().registRegister(
			Constant.EVENT_TYPE_MODEL/*�����Ӧevent�е�registerType������event������registerType�����ͨ��������ҵ���Ӧ��ע����
			��������type�����������ж��壬��ʱevent��д��ʱ���Ӧ�Ϳ�����*/,
			ModelFactory.getRegister()/*���Լ�������*/);
			
		//�������ע�������ɿ���ڲ��ṩ�ģ���Ҫ��������������activity�����������͹㲥����������
        EventFactory.getEventRegisterFactory().registRegister(
		Constant.EVENT_TYPE_CONTEXT,
		ContextReceiver.getRegisterInstance());
		
        //Ϊҵ�񹤳�����ַ���������Ӧtype��ע�����ṩ�ַ�����ע������ĵ�һ������typeҪ��ע����Ǹ�ע������Ӧ��
        EventFactory.getEventRegisterFactory().registDispatcher(Constant.EVENT_TYPE_MODEL, new DefaultEventDispatcher());
        EventFactory.getEventRegisterFactory().registDispatcher(Constant.EVENT_TYPE_CONTEXT, new ContextEventDispatcher());
		
		������ע�Ṥ�������ˣ�������δ��뽨��д��Application���У�ͨ������ע�᷽ʽʵ��Event���ƿ��Խ����Լ���Ŀ��ҵ��ģ���
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
		EventBuilder.Event<EventJsonObject> event = new EventBuilder()
			.type(Constant.EVENT_TYPE_MODEL)//��д��ע����������
			.key(Constant.MAIN_MODEL)//ע����ͨ�����key�ҵ���Ӧ�Ľ��������̳���EventRegister�ӿڵ�ע������ʵ��getReceiver������
			//��������Ĳ���ֻ��һ�����������ﴫ��ȥ��key��
			.requestId(0)
			.startTime(System.currentTimeMillis())
			.target(EventHandler.getInstance())
			.requestBundle(bundle)
			.callback(new EventCallback<EventJsonObject>() {
				@Override
				public  void call(EventBuilder.Event<EventJsonObject> event) {
				
					parseData(event);
					
				}
			}).subscribeOn(Schedulers.cache())
			.observeOn(Schedulers.ui())
			.build();

		event.send();
		
		
		
		
		