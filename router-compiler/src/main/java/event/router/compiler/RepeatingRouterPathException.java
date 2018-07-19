package event.router.compiler;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2018/7/18 0018
 * @modifyDate : 2018/7/18 0018
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public class RepeatingRouterPathException extends RuntimeException {

    public RepeatingRouterPathException() {
        super();
    }

    public RepeatingRouterPathException(String message) {
        super(message);
    }

    public RepeatingRouterPathException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepeatingRouterPathException(Throwable cause) {
        super(cause);
    }

    public RepeatingRouterPathException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
