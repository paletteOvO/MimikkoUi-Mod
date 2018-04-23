package me.manhong2112.xposed.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.xposed.annotation.HookAfter
import me.manhong2112.xposed.annotation.HookClass
import me.manhong2112.xposed.annotation.HookPackage
import me.manhong2112.xposed.util.GeneratedUtils
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic


@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("me.manhong2112.xposed.annotation.HookAfter", "me.manhong2112.xposed.annotation.HookPackage")
class HookProcessor : AbstractProcessor() {
   fun Messager.note(msg: String) {
      this.printMessage(Diagnostic.Kind.NOTE, msg)
   }

   private lateinit var messager: Messager
   private lateinit var filer: Filer

   private val packageName = "me.manhong2112.xposed"
   val methodSpec = FunSpec.builder("handleLoadPackage")
         .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)
         .addParameter("lpparam", XC_LoadPackage.LoadPackageParam::class)

   override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
      if (roundEnv.processingOver()) {
         finishProcess()
      } else {
         roundEnv.getElementsAnnotatedWith(HookAfter::class.java).forEach { ele ->
            val packageElement = getPackageElement(ele)
            val classElement = run {
               val c = getClassElement(ele)
               if (c.simpleName.toString() == "Companion") {
                  getClassElement(c.enclosingElement)
               } else {
                  c
               }
            }
            val hookAfterAnno = ele.getAnnotation(HookAfter::class.java)
            val packageAnno: HookPackage? = ele.getAnnotation(HookPackage::class.java)
            val classAnno = ele.getAnnotation(HookClass::class.java)

            if (packageAnno === null) {
               methodSpec
                     .beginControlFlow("run")
                     .addStatement("""
                        |%T.hook(priority = ${hookAfterAnno.priority}, %T.findMethod(%T.findClass("${classAnno.className}", lpparam.classLoader), "${hookAfterAnno.method}", ${hookAfterAnno.argsType.joinToString(", ") {
                        "%T.findClass(\"$it\", lpparam.classLoader)"
                     }}), priority = ${hookAfterAnno.priority}) {
                        |   ${packageElement.qualifiedName}.${classElement.simpleName}.${ele.simpleName}(lpparam)
                        | }
                     """.trimMargin(), GeneratedUtils::class, GeneratedUtils::class, XposedHelpers::class, XposedHelpers::class)
                     .endControlFlow()
            } else {
               methodSpec.beginControlFlow("if(lpparam.packageName == %S)", packageAnno.packageName)
                     .addStatement("""
                        |%T.hook(%T.findMethod(%T.findClass("${classAnno.className}", lpparam.classLoader), "${hookAfterAnno.method}", ${hookAfterAnno.argsType.joinToString(", ") {
                        "%T.findClass(\"$it\", lpparam.classLoader)"
                     }}), priority = ${hookAfterAnno.priority}) {
                        |   ${packageElement.qualifiedName}.${classElement.simpleName}.${ele.simpleName}(lpparam)
                        | }
                     """.trimMargin(), GeneratedUtils::class, GeneratedUtils::class, XposedHelpers::class, XposedHelpers::class)
                     .endControlFlow()
            }
         }
      }
      return true
   }

   private tailrec fun getPackageElement(ele: Element): PackageElement {
      return if (ele.kind == ElementKind.PACKAGE) {
         ele as PackageElement
      } else {
         getPackageElement(ele.enclosingElement)
      }
   }

   private tailrec fun getClassElement(ele: Element): Element {
      return if (ele.kind == ElementKind.CLASS) {
         ele
      } else {
         getClassElement(ele.enclosingElement)
      }
   }

   private fun finishProcess() {
      val sourceDir = processingEnv.options["kapt.kotlin.generated"] ?: run {
         messager.printMessage(Diagnostic.Kind.ERROR, "kapt generate dir not found")
         return
      }
      val assetPath = sourceDir.substring(0, sourceDir.indexOf("build\\generated\\source")) + "src\\main\\assets\\xposed_init"

      val xposedInitCls = TypeSpec.classBuilder(ClassName(packageName, "GeneratedXposedInit"))
            .addSuperinterface(IXposedHookLoadPackage::class)
            .addFunction(methodSpec.build())
            .build()

      val file = FileSpec.builder(packageName, "GeneratedXposedInit.kt")
            .addType(xposedInitCls)
            .build()

      // write source
      File("$sourceDir/${packageName.replace(".", "/")}", file.name).apply {
         parentFile.mkdirs()
         writeText(file.toString())
      }

      // write util
      File("$sourceDir/${"me.manhong2112.xposed.util".replace(".", "/")}", "GeneratedUtils.kt").apply {
         parentFile.mkdirs()
         writeText("""package me.manhong2112.xposed.util

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

object GeneratedUtils {
   object CallOriginalMethod : Throwable()

   fun findMethod(cls: Class<*>, methodName: String, vararg typeList: Class<*>): Method {
      return try {
         XposedHelpers.findMethodExact(cls, methodName, *typeList).also {
            it.isAccessible = true
         }
      } catch (e: NoSuchMethodException) {
         cls.superclass?.let {
            findMethod(cls.superclass, methodName, *typeList)
         } ?: throw e
      } catch (e: NoSuchMethodError) {
         cls.superclass?.let {
            findMethod(cls.superclass, methodName, *typeList)
         } ?: throw e
      }
   }

   fun findMethod(obj: Any, methodName: String, vararg typeList: Class<*>): Method {
      return findMethod(obj::class.java, methodName, *typeList)
   }

   inline fun hookAllMethods(cls: Class<*>, methodName: String, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}) {
      XposedBridge.hookAllMethods(cls, methodName, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
   }

   inline fun hook(m: Method, priority: Int = 50, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      m.isAccessible = true
      val hook = XposedBridge.hookMethod(m, object : XC_MethodHook(priority) {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
      return hook
   }

   inline fun <T> hook(c: Constructor<T>, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}) {
      c.isAccessible = true
      XposedBridge.hookMethod(c, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
   }

   inline fun hookAllMethod(cls: Class<*>, name: String, crossinline before: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }, crossinline after: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }) {
      cls.declaredMethods.filter { it.name == name }.map { hook(it, before = { p -> before(it, p) }, after = { p -> after(it, p) }) }
   }

   inline fun replace(m: Method, crossinline replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      m.isAccessible = true
      return replaceMethod(m, replacement = replacement)
   }
   inline fun replaceMethod(method: Member, crossinline replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      return XposedBridge.hookMethod(method, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            try {
               val result = replacement(param)
               param.result = result
            } catch (_: CallOriginalMethod) {
            }
         }
      })
   }

}
""".trimIndent())
      }

      //write xposed_init
      File(assetPath).apply {
         writeText(readLines().toMutableSet().apply { add("$packageName.${xposedInitCls.name}") }.joinToString("\n"))
      }
   }

   @Synchronized
   override fun init(processingEnv: ProcessingEnvironment) {
      messager = processingEnv.messager
      filer = processingEnv.filer
      this.processingEnv = processingEnv
   }
}
