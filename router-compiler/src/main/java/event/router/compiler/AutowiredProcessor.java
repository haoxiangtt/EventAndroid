package event.router.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

import event.router.annotation.Autowired;

/**
 * <pre>
 * @copyright  : Copyright ©2004-2018 版权所有　XXXXXXXXXXXXXXXXXXXXXXX
 * @company    : XXXXXXXXXXXXXXXXXXXXXX
 * @author     : OuyangJinfu
 * @e-mail     : ouyangjinfu@richinfo.cn
 * @createDate : 2018/7/17 0017
 * @modifyDate : 2018/7/17 0017
 * @version    : 1.0
 * @desc       :
 * </pre>
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedAnnotationTypes("event.router.annotation.Autowired")
public class AutowiredProcessor extends AbstractProcessor {

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
//        mMessager.printMessage(Diagnostic.Kind.WARNING, ">>>>>>>>>AutowiredProcessor begin<<<<<<<<<");
//        mMessager.printMessage(Diagnostic.Kind.WARNING, "annotations size = " + annotations.size());
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Autowired.class);
//        mMessager.printMessage(Diagnostic.Kind.WARNING, "autowired elements size = " + elements.size());
        if (elements.size() == 0) return true;
        Map<String, Set<Map<String, String>>> autowiredMap = generateAutowiredMap(elements);
        try {
            generateClassFile(autowiredMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean generateClassFile(Map<String, Set<Map<String, String>>> autowiredMap) {
        for (Map.Entry<String, Set<Map<String, String>>> entry : autowiredMap.entrySet()) {
            try {
//                Class<?> parentCls = Class.forName(entry.getKey());
                String clsName = entry.getKey();
                String pkgName = clsName.substring(0, clsName.lastIndexOf("."));
                String simpleName = clsName.substring(clsName.lastIndexOf(".") + 1, clsName.length());

                //创建属性
                FieldSpec targetField = FieldSpec.builder(ClassName.bestGuess(clsName), "mTarget"
                    , Modifier.PRIVATE).build();

                //创建构造方法
                MethodSpec constructor = null;
                MethodSpec.Builder ctrBuilder =  MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.bestGuess(clsName), "target")
                    .addStatement("this.$N = target", targetField);
                for (Map<String, String> map : entry.getValue()) {
                    String type = map.get("type");
                    String name = map.get("name");
                    String path = map.get("path");
                    String singleton = map.get("singleton");
                    String typeParam = map.get("typeParam");
                    if ("field".equals(type)) {
                        ctrBuilder.addStatement("$N.$N = $T.findRequiredField($S, $S, $S, $N, $N)"
                            , targetField, name, ClassName.bestGuess("event.router.Utils")
                            , type, name, path, singleton, "target");
                    } else if ("method".equals(type)){
                        ctrBuilder.addStatement("$N.$N(($T)$T.findRequiredField($S, $S, $S, $N, $N))"
                            , targetField, name,ClassName.bestGuess(typeParam)
                            , ClassName.bestGuess("event.router.Utils")
                            , type, name, path, singleton, "target");
                    }
                }

                constructor = ctrBuilder.build();

                //创建release方法
                MethodSpec releaseMethod = null;
                MethodSpec.Builder rlsBuilder = MethodSpec.methodBuilder("release")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC);
                for (Map<String, String> map : entry.getValue()) {
                    String type = map.get("type");
                    String name = map.get("name");
                    if ("field".equals(type)) {
                        rlsBuilder.addStatement("$N.$N = null"
                            , targetField, name);
                    } else if ("method".equals(type)){
                        rlsBuilder.addStatement("$N.$N(null)"
                                , targetField, name);
                    }
                }
                releaseMethod = rlsBuilder.build();

                //创建类
                TypeSpec injector = TypeSpec.classBuilder(simpleName + "_Injector")
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addSuperinterface(ClassName.bestGuess("event.router.interfaces.EventRelease"))
                    .addField(targetField)
                    .addMethod(constructor)
                    .addMethod(releaseMethod)
                    .build();

                JavaFile.builder(pkgName, injector).build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return true;
    }

    private Map<String, Set<Map<String, String>>> generateAutowiredMap(Set<? extends Element> elements) {
        Map<String, Set<Map<String, String>>> autowiredMap = new HashMap<>();
        for (Element element : elements) {
            Autowired autoWired = element.getAnnotation(Autowired.class);
            String path = autoWired.path();
            String singleton = Boolean.valueOf(autoWired.singleton()).toString();
            TypeElement parentElement = (TypeElement) element.getEnclosingElement();
            String typeClsName = parentElement.getQualifiedName().toString();
            if (autowiredMap.get(typeClsName) == null) {
                Set<Map<String, String>> set = new HashSet<>();
                autowiredMap.put(typeClsName, set);
            }
            String name = element.getSimpleName().toString();
            if (element.getKind().isField()) {
                Map<String, String> map = new HashMap<>();
                map.put("type", "field");
                map.put("name", name);
                map.put("path", path);
                map.put("singleton", singleton);
                TypeElement typeElement = (TypeElement)((DeclaredType)element.asType()).asElement();
                map.put("typeParam", typeElement.getQualifiedName().toString());
                Set<Map<String, String>> set = autowiredMap.get(typeClsName);
                set.add(map);
            } else if (element.getKind() == ElementKind.METHOD) {
                Map<String, String> map = new HashMap<>();
                map.put("type", "method");
                map.put("name", name);
                map.put("path", path);
                map.put("singleton", singleton);
                ExecutableElement execElement = (ExecutableElement) element;
                List<? extends VariableElement> paramTypeList = execElement.getParameters();

                if (paramTypeList.size() == 1) {
                    TypeElement typeElement = (TypeElement) ((DeclaredType)paramTypeList.get(0).asType()).asElement();
                    map.put("typeParam", typeElement.getQualifiedName().toString());
                } else {
                    throw new RuntimeException("the method(" + name + ") argument length is " + paramTypeList.size()
                        + ", must only have one argument");
                }
                Set<Map<String, String>> set = autowiredMap.get(typeClsName);
                set.add(map);
            }
        }
        return autowiredMap;
    }
}
