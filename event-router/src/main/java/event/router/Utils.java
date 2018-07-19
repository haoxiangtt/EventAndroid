package event.router;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import event.base.EventConfig;
import event.router.interfaces.EventMaker;
import event.router.interfaces.EventRelease;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2018/6/20 0020
 * @modifyDate : 2018/6/20 0020
 * @version    : 1.0
 * @desc       :
 * </pre>
 */

public class Utils {

    private static final boolean DEBUG = EventConfig.isDebugMode();

    private static final String ROUTER_MAPPER_CLASS = "event.router.RouterMapper";

    private static final Map<String, Map<String, Object>> CACHE_ROUTER_MAP = new WeakHashMap<>();

    private static final Map<Class<?>, Constructor<? extends EventRelease>> CACHE_INJECTOR_MAP = new WeakHashMap<>();

    static boolean initializeRouterMapper(){

        try {
            Class<?> routerMapperCls = Class.forName(ROUTER_MAPPER_CLASS);
            Method initMethod = routerMapperCls.getMethod("init");
            initMethod.invoke(null);
            return true;
        } catch (NoSuchMethodException e) {
            if (DEBUG) e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            if (DEBUG) e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            if (DEBUG) e.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            if (DEBUG) e.printStackTrace();
            return false;
        }
    }

    static void release(){
        try {
            Class<?> routerMapperCls = Class.forName(ROUTER_MAPPER_CLASS);
            Method initMethod = routerMapperCls.getMethod("release");
            initMethod.invoke(null);
            CACHE_ROUTER_MAP.clear();
            CACHE_INJECTOR_MAP.clear();
            EventMaker.SINGLETON_MAP.clear();
        } catch (NoSuchMethodException e) {
            if (DEBUG) e.printStackTrace();
        } catch (ClassNotFoundException e) {
            if (DEBUG) e.printStackTrace();
        } catch (IllegalAccessException e) {
            if (DEBUG) e.printStackTrace();
        } catch (InvocationTargetException e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    public static Map<String, Object> findRouteInfoFromPath(String path) {
        Map<String, Object> cache = CACHE_ROUTER_MAP.get(path);
        if (cache != null) {
            return cache;
        } else {
            try {
                Class<?> routerMapperCls = Class.forName(ROUTER_MAPPER_CLASS);
                Method getMethod = routerMapperCls.getDeclaredMethod("get", String.class);
                Map<String, String> infoMap = (Map<String, String>)getMethod.invoke(null, path);
                Class<?> componentCls = Class.forName(infoMap.get("path"));
                Map<String, Object> newCache = new HashMap<>();
                newCache.put("type", infoMap.get("type"));
                newCache.put("path", componentCls);
                CACHE_ROUTER_MAP.put(path, newCache);
                return newCache;
            } catch (ClassNotFoundException e) {
                if (DEBUG) e.printStackTrace();
                return null;
            } catch (NoSuchMethodException e) {
                if (DEBUG) e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                if (DEBUG) e.printStackTrace();
                return null;
            } catch (InvocationTargetException e) {
                if (DEBUG) e.printStackTrace();
                return null;
            }
        }
    }

    public static <T> T findRequiredField(String type, String name, String path, boolean singleton
            , Object taget) {
        if ("field".equals(type)) {
            AutowiredField autoWiredField = new AutowiredField(name, path, singleton);
            return (T)autoWiredField.newInstants(taget);
        } else if ("method".equals(type)) {
            AutowiredMethod autowiredMethod = new AutowiredMethod(name, path, singleton);
            return (T)autowiredMethod.newInstants(taget);
        }
        return null;
    }

    public static Constructor<? extends EventRelease> findInjectorConstructorForClass(Class<?> cls) {
        if (cls == null) return null;
        Constructor<? extends EventRelease> injectCtr = CACHE_INJECTOR_MAP.get(cls);
        if (injectCtr != null) {
            if (DEBUG) Log.d("ERounter", "HIT: Cached in binding map.");
            return injectCtr;
        }
        String clsName = cls.getName();
        if (clsName.startsWith("android.") || clsName.startsWith("java.")) {
            if (DEBUG) Log.d("ERounter", "MISS: Reached framework class. Abandoning search.");
            return null;
        }
        try {
            Class<?> injectClass = cls.getClassLoader().loadClass(clsName + "_Injector");
            //noinspection unchecked
            injectCtr = (Constructor<? extends EventRelease>) injectClass.getConstructor(cls);
            if (DEBUG) Log.d("ERounter", "HIT: Loaded binding class and constructor.");
        } catch (ClassNotFoundException e) {
            if (DEBUG) Log.d("ERounter", "Not found. Trying superclass " + cls.getSuperclass().getName());
            injectCtr = findInjectorConstructorForClass(cls.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
        }
        if (injectCtr != null) {
            CACHE_INJECTOR_MAP.put(cls, injectCtr);
        }
        return injectCtr;
    }
}
