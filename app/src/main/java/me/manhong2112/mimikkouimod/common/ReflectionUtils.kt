package me.manhong2112.mimikkouimod.common

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import org.jetbrains.anko.doAsync
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

@SuppressWarnings("unchecked")
object ReflectionUtils {
   object CallOriginalMethod : Throwable()

   fun Any.printAllField() {
      val obj = this
      Utils.log(obj::class.java.canonicalName)
      obj::class.java.declaredFields.map {
         it.isAccessible = true
         Utils.log("  ${it.name} :: ${it.type.canonicalName} : ${it.type.superclass?.canonicalName} = ${it.get(obj)}")
      }
   }


   inline fun hookMethodAsync(priority: Int = 50, method: Member, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      return XposedBridge.hookMethod(method, object : XC_MethodHook(priority) {
         override fun beforeHookedMethod(param: MethodHookParam) {
            doAsync {
               before(param)
            }
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            doAsync {
               after(param)
            }
         }
      })
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

   fun <T> Any.getField(name: String): T {
      return XposedHelpers.getObjectField(this, name) as T
   }

   fun Any.setField(name: String, value: Any) {
      XposedHelpers.setObjectField(this, name, value)
   }

   fun <T> Any.invokeMethod(name: String, vararg args: Any): T {
      return this::class.java.findMethod(name, *args.map {
         when (it) {
            is Byte -> java.lang.Byte.TYPE
            is Short -> java.lang.Short.TYPE
            is Int -> java.lang.Integer.TYPE
            is Long -> java.lang.Long.TYPE
            is Float -> java.lang.Float.TYPE
            is Double -> java.lang.Double.TYPE
            is Boolean -> java.lang.Boolean.TYPE
            is Char -> java.lang.Character.TYPE
            else -> it::class.java
         }
      }.toTypedArray()).invoke(this, *args) as T
   }

   fun Class<*>.findMethod(methodName: String, vararg typeList: Class<*>): Method {
      val cls = this
      return try {
         XposedHelpers.findMethodExact(cls, methodName, *typeList).also {
            it.isAccessible = true
         }
      } catch (e: NoSuchMethodException) {
         this.superclass?.findMethod(methodName, *typeList) ?: throw e
      } catch (e: NoSuchMethodError) {
         this.superclass?.findMethod(methodName, *typeList) ?: throw e
      }
   }

   fun Any.findMethod(methodName: String, vararg typeList: Class<*>): Method {
      return this.javaClass.findMethod(methodName, *typeList)
   }

   inline fun Class<*>.hookAllMethods(methodName: String, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}) {
      XposedBridge.hookAllMethods(this, methodName, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
   }

   inline fun Method.hook(priority: Int = 50, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      val hook = XposedBridge.hookMethod(this, object : XC_MethodHook(priority) {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
      return hook
   }

   inline fun Method.hookAsync(priority: Int = 50, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return hookMethodAsync(priority, this, before = before, after = after)
   }

   inline fun <T> Constructor<T>.hook(crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}) {
      this.isAccessible = true
      XposedBridge.hookMethod(this, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
   }

   inline fun Class<*>.hookAllMethod(name: String, crossinline before: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }, crossinline after: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }) {
      this.declaredMethods.filter { it.name == name }.map { it.hook(before = { p -> before(it, p) }, after = { p -> after(it, p) }) }
   }

   fun Method.replace(replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return replaceMethod(this, replacement = replacement)
   }
}