package event.router.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import event.router.annotation.Router;

/**
 * <pre>
 * copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXX
 * company    : XXXXXXXXXXXXXXXXXXXXXXXXx
 * @author     : OuyangJinfu
 * e-mail     : jinfu123.-@163.com
 * createDate : 2018/7/17 0017
 * modifyDate : 2018/7/17 0017
 * @version    : 1.0
 * desc       :
 * </pre>
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("event.router.annotation.Router")
public class RouterProcessor extends AbstractProcessor{
    private static final String PACKAGE_NAME = "event.router";
    private Filer mFiler; //文件相关的辅助类
    private Elements mElementUtils; //元素相关的辅助类
    private Messager mMessager; //日志相关的辅助类
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
//        mMessager.printMessage(Diagnostic.Kind.WARNING, ">>>>>>>>>init<<<<<<<<<");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        mMessager.printMessage(Diagnostic.Kind.WARNING, ">>>>>>>>>RouterProcessor begin<<<<<<<<<");
//        mMessager.printMessage(Diagnostic.Kind.WARNING, "annotations size = " + annotations.size());
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Router.class);
//        mMessager.printMessage(Diagnostic.Kind.WARNING, "Router elements size = " + elements.size());
        if (elements.size() == 0) return true;
        Map<String,Map<String, String>> routerMap = generateRounterMap(elements);
        try {
            generateFile(routerMap).writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    JavaFile generateFile(Map<String, Map<String, String>> routerMap) {
        ParameterizedTypeName innerPTN = ParameterizedTypeName.get(Map.class, String.class, String.class);
        ParameterizedTypeName pRouterMapper = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ParameterizedTypeName.get(String.class),
                innerPTN);
        FieldSpec mapperField = FieldSpec.builder(pRouterMapper,
            "mRouterMapper", Modifier.PRIVATE, Modifier.STATIC)
                .build();

        MethodSpec.Builder initMethodBuilder = MethodSpec.methodBuilder("init")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addStatement("mRouterMapper = new $T<>()", ClassName.get(HashMap.class));
        initMethodBuilder.addStatement("$T<String, String> infoMap = null", Map.class);
        for (Map.Entry<String, Map<String, String>> stringClassEntry : routerMap.entrySet()) {
            Map<String, String> infoMap = stringClassEntry.getValue();
            initMethodBuilder
                .addStatement("infoMap = new $T<>()", HashMap.class)
                .addStatement("infoMap.put($S, $S)", "path", infoMap.get("path"))
                .addStatement("infoMap.put($S, $S)", "type", infoMap.get("type"))
                .addStatement("mRouterMapper.put($S,infoMap)",
                stringClassEntry.getKey());
        }
        MethodSpec initMethod = initMethodBuilder.build();
        MethodSpec getMethod = MethodSpec.methodBuilder("get")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addParameter(String.class, "key")
            .returns(innerPTN)
            .addStatement("return mRouterMapper.get(key)").build();

        MethodSpec releaseMethod = MethodSpec.methodBuilder("release")
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
            .addStatement("$N.clear()", mapperField).build();

        TypeSpec mapperClass = TypeSpec.classBuilder("RouterMapper")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(mapperField)
            .addMethod(initMethod)
            .addMethod(getMethod)
            .addMethod(releaseMethod)
            .build();
        return JavaFile.builder(PACKAGE_NAME, mapperClass).build();
    }

    private Map<String,Map<String, String>> generateRounterMap(Set<? extends Element> elements) {
        Map<String,Map<String, String>> routerMap = new HashMap<>();
        for (Element element : elements) {
            Router router = element.getAnnotation(Router.class);
            String key = router.path();
            String className = ((TypeElement)element).getQualifiedName().toString();
            Map<String, String> historyMap = routerMap.get(key);
            if (historyMap != null) {
                throw new RepeatingRouterPathException("this path(" + key + "->"
                    + className
                    + ") was repeated; already defined path("
                    + key + "->"
                    + historyMap.get("path") + ").");
            }
            Map<String, String> infoMap = new HashMap<>();
            infoMap.put("path", className);
            infoMap.put("type", String.valueOf(router.type()));
            routerMap.put(key, infoMap);

        }
        return routerMap;
    }

    private void error(String msg, Object... args) {
        mMessager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
    }
}
