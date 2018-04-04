package me.manhong2112.mimikkouimod.common

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import org.jetbrains.anko.doAsync
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method

object ReflectionUtils {
   fun Any.printAllField() {
      val obj = this
      Utils.log(obj::class.java.canonicalName)
      obj::class.java.declaredFields.map {
         it.isAccessible = true
         Utils.log("  ${it.name} :: ${it.type.canonicalName} : ${it.type.superclass?.canonicalName} = ${it.get(obj)}")
      }
   }


   fun hookMethod(method: Member, before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      return XposedBridge.hookMethod(method, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
   }

   fun hookMethodAsync(method: Member, before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      return XposedBridge.hookMethod(method, object : XC_MethodHook() {
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

   fun replaceMethod(method: Member, replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      return XposedBridge.hookMethod(method, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            try {
               val result = replacement(param)
               param.result = result
            } catch (_: Utils.CallOriginalMethod) {
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

   fun Class<*>.hookAllMethods(methodName: String, before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}) {
      XposedBridge.hookAllMethods(this, methodName, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
   }

   fun Method.hook(before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return hookMethod(this, before = before, after = after)
   }

   fun Method.hookAsync(before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return hookMethodAsync(this, before = before, after = after)
   }

   fun <T> Constructor<T>.hook(before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}) {
      hookMethod(this, before, after)
   }

   fun Class<*>.hookAllMethod(name: String, before: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }, after: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }) {
      this.declaredMethods.filter { it.name == name }.map { it.hook(before = { p -> before(it, p) }, after = { p -> after(it, p) }) }
   }

   fun Method.replace(replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return replaceMethod(this, replacement = replacement)
   }
}