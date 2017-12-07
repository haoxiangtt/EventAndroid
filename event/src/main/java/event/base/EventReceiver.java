package event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/4/14 0014
 * @modifyDate : 2017/4/14 0014
 * @version    : 1.0
 * @desc       : 接收者(接收器)
 * </pre>
 */

public interface EventReceiver<V, T> {
    void onReceive(EventBuilder.Event<V, T> event);
}
