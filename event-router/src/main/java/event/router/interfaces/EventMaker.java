package event.router.interfaces;

import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2018/7/17 0017
 * modifyDate : 2018/7/17 0017
 * @version    : 1.0
 * desc       :
 * </pre>
 */

public interface EventMaker {
    Map<String, Object> SINGLETON_MAP = new HashMap<>();
    Object newInstants(Object taget);
}
