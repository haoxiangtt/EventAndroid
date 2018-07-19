package cn.richinfo.eventandroid;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import event.router.annotation.Router;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * @company    : 彩讯科技股份有限公司
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2018/6/21 0021
 * @modifyDate : 2018/6/21 0021
 * @version    : 1.0
 * @desc       :
 * </pre>
 */
@Router(path = "/test/myservice", type = Router.Type.COMPONENT_SERVICE)
public class TestService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("event-android", "----------my test service start.-----------");
        Toast.makeText(this,
                "my test service start.",Toast.LENGTH_SHORT).show();
        return super.onStartCommand(intent, flags, startId);
    }

}
