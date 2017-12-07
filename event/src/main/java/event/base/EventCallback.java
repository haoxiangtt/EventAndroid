package event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技有限公司
 * @company    : 彩讯科技有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/18
 * @modifyDate : 2017/4/18
 * version     : 1.0
 * @desc       : 事件回调接口(观察者接口)
 * </pre>
 */

public interface EventCallback<V, T> {
     void call(EventBuilder.Event<V, T> event);
}
