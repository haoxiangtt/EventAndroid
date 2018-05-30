package event;

import event.base.BaseEventDispatcher;
import event.base.EventBuilder;
import event.base.Subscription;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2017/4/18
 * modifyDate : 2017/4/18
 * @version    : 2.0
 * desc       : 默认的事件分发器, 沿用BaseEventDispatcher逻辑, 暂为做实现
 * //TODO 待实现
 * </pre>
 */

public class DefaultEventDispatcher extends BaseEventDispatcher {

    @Override
    public Subscription dispatch(EventBuilder.Event event) {
        return super.dispatch(event);
    }

    @Override
    protected Subscription onSchedule(EventBuilder.Event event) {
        return super.onSchedule(event);
    }
}
