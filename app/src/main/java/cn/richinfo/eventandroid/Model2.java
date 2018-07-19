package cn.richinfo.eventandroid;

import android.content.Context;
import android.widget.Toast;

import event.router.annotation.Router;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2018/7/18 0018
 * @modifyDate : 2018/7/18 0018
 * @version    : 1.0
 * @desc       :
 * </pre>
 */
@Router(path = "/test/model2", type = Router.Type.COMPONENT_MODEL)
public class Model2 {
    public void show(Context context, String msg) {
        Toast.makeText(context, msg + "->我的hashCode=" + hashCode(), Toast.LENGTH_SHORT).show();
    }
}
