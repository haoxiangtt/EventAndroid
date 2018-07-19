package event.router.interfaces;

import android.support.annotation.UiThread;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXXx
 * @author     : OuyangJinfu
 * e-mail     : ouyangjinfu@richinfo.cn
 * createDate : 2018/7/17 0017
 * modifyDate : 2018/7/17 0017
 * @version    : 1.0
 * desc       :
 * </pre>
 */

public interface EventRelease {

    @UiThread void release();

    EventRelease EMPTY = new EventRelease() {
        @Override
        public void release() {}
    };
}
