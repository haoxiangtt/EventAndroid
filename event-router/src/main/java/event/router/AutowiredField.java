package event.router;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import event.router.annotation.Router;
import event.router.interfaces.EventMaker;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2018/7/17 0017
 * modifyDate : 2018/7/17 0017
 * @version    : 1.0
 * desc       :
 * </pre>
 */

class AutowiredField implements EventMaker {
    private boolean singleton = false;
    private String name= "";
    private String path = "";

    public AutowiredField(String name, String path, boolean singleton) {
        this.name = name;
        this.path = path;
        this.singleton = singleton;
    }


    @Override
    public Object newInstants(Object taget) {
        try {
            Class cls = taget.getClass();
            Field field = cls.getDeclaredField(name);
            field.setAccessible(true);
            if (TextUtils.isEmpty(path)) {
                Class<?> typeCls = field.getType();
                if (!singleton) {
                    return typeCls.newInstance();
                } else {
                    Object instance = SINGLETON_MAP.get(typeCls.getCanonicalName());
                    if (instance == null) {
                        instance = typeCls.newInstance();
                        SINGLETON_MAP.put(typeCls.getCanonicalName(), instance);
                    }
                    return instance;

                }
            } else {
                Class<?> routerMapperCls = Class.forName("event.router.RouterMapper");
                Method getMethod = routerMapperCls.getDeclaredMethod("get", String.class);
                Map<String, String> map = (Map<String, String>)getMethod.invoke(null, path);
                if (map == null) {
                    Log.e("ERounter", ">>>>there was no router path:" + path);
                    return null;
                }
                String clsName = map.get("path");
                String routerType = map.get("type");
                if (Integer.valueOf(routerType) == Router.Type.COMPONENT_MODEL) {
                    if (!singleton) {
                        return Class.forName(clsName).newInstance();
                    } else {
                        Object instance = SINGLETON_MAP.get(clsName);
                        if (instance == null) {
                            instance = Class.forName(clsName).newInstance();
                            SINGLETON_MAP.put(clsName, instance);
                        }
                        return instance;
                    }
                } else {
                    Log.e("ERounter", ">>>>>the path:\""+ path
                            + "\" type is not a model type, cannot instanting<<<<<");
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
