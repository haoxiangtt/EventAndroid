package event.base;

import event.BuildConfig;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2018/7/18 0018
 * modifyDate : 2018/7/18 0018
 * @version    : 1.0
 * desc       :
 * </pre>
 */

public class EventConfig {

    private static boolean DEBUG = BuildConfig.DEBUG;

    public static void setDebugMode(boolean isDebug) {
        DEBUG = isDebug;
    }

    public static boolean isDebugMode(){
        return DEBUG;
    }

}
