@file:Suppress("UNCHECKED_CAST")

package me.manhong2112.mimikkouimod.common

import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Constructor
import java.lang.reflect.Method

object CallOriginalMethod : Throwable()

fun Any.printAllField() {
   val obj = this
   Utils.log(obj::class.java.canonicalName)
   (obj::class.java.declaredFields
         + obj::class.java.fields).toSet()
         .map {
      it.isAccessible = true
      Utils.log("  ${it.name} :: ${it.type.canonicalName} : ${it.type.superclass?.canonicalName} = ${it.get(obj)}")
   }
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

inline fun Class<*>.hookAllMethods(methodName: String, crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}): Set<XC_MethodHook.Unhook> {
   return XposedBridge.hookAllMethods(this, methodName, object : XC_MethodHook() {
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
   return XposedBridge.hookMethod(this, object : XC_MethodHook(priority) {
      override fun beforeHookedMethod(param: MethodHookParam) {
         before(param)
      }

      override fun afterHookedMethod(param: MethodHookParam) {
         after(param)
      }
   })
}

inline fun Constructor<*>.hook(crossinline before: (XC_MethodHook.MethodHookParam) -> Unit = {}, crossinline after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
   this.isAccessible = true
   return XposedBridge.hookMethod(this, object : XC_MethodHook() {
      override fun beforeHookedMethod(param: MethodHookParam) {
         before(param)
      }

      override fun afterHookedMethod(param: MethodHookParam) {
         after(param)
      }
   })
}

inline fun Method.replace(crossinline replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
   this.isAccessible = true
   return XposedBridge.hookMethod(this, object : XC_MethodHook() {
      override fun beforeHookedMethod(param: MethodHookParam) {
         try {
            val result = replacement(param)
            param.result = result
         } catch (_: CallOriginalMethod) {
         }
      }
   })
}
