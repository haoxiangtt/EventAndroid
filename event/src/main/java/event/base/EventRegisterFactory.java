package event.base;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技有限公司
 * @company    : 彩讯科技有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2017/6/2 0002
 * @modifyDate : 2017/6/2 0002
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public interface EventRegisterFactory {

    EventRegisterFactory bindRegister(int type, EventRegister register);

    EventRegister getRegister(int type);

    EventRegisterFactory bindDispatcher(int type, EventDispatcher dispatcher);

    EventDispatcher getDispatcher(int type);
}
