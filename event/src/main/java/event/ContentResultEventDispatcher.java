package event;

import event.base.BaseEventDispatcher;
import event.base.EventBuilder;
import event.base.Subscription;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/6/2 0002
 * @modifyDate : 2017/6/2 0002
 * @version    : 1.0
 * @desc       : 从本地数据库或者从内容提供者提取数据时用到的事件分发器，暂未实现。
 * //TODO 待实现
 * </pre>
 */

public class ContentResultEventDispatcher extends BaseEventDispatcher {

    public ContentResultEventDispatcher() {

    }

    @Override
    public Subscription dispatch(EventBuilder.Event event) {
        return super.dispatch(event);
    }

}
