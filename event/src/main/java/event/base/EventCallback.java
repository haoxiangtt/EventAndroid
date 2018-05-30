package event.base;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2017/4/18
 * modifyDate : 2017/4/18
 * version     : 1.0
 * desc       : 事件回调接口(观察者接口)
 * </pre>
 */

public interface EventCallback<V, T> {
     void call(EventBuilder.Event<V, T> event);
}
