package event.router;

import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import event.router.annotation.Router;
import event.router.interfaces.EventMaker;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　彩讯科技股份有限公司
 * company    : 彩讯科技股份有限公司
 * author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2018/7/17 0017
 * modifyDate : 2018/7/17 0017
 * @version    : 1.0
 * desc       :
 * </pre>
 */

class AutowiredMethod implements EventMaker {

    private String name= "";
    private String path = "";
    private boolean singleton = false;

    public AutowiredMethod(String name, String path, boolean singleton) {
        this.name = name;
        this.path = path;
        this.singleton = singleton;
    }

    @Override
    public Object newInstants(Object taget) {
        try {
            Class cls = taget.getClass();
            Method[] methods = cls.getDeclaredMethods();
            Method method = null;
            for (Method m : methods) {
                if (name.equals(m.getName()) && m.getParameterTypes().length == 1){
                    method = m;
                }
            }
            if (method == null) {
                throw new NoSuchMethodException();
            }
            method.setAccessible(true);
            if (TextUtils.isEmpty(path)) {
                Class<?>[] typeClss = method.getParameterTypes();
                if (typeClss.length == 1) {
                    if (!singleton) {
                        return typeClss[0].newInstance();
                    } else {
                        Object instance = SINGLETON_MAP.get(typeClss[0].getCanonicalName());
                        if (instance == null) {
                            instance = typeClss[0].newInstance();
                            SINGLETON_MAP.put(typeClss[0].getCanonicalName(), instance);
                        }
                        return instance;

                    }
                } else {
                    Log.e("ERounter", ">>>the method argument length error, must only have one arggument<<<<");
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
                    Log.e("ERounter", ">>>>>the path:\"" + path
                            + "\" type is not a model type, cannot instanting<<<<<");
                }
            }
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
