package me.manhong2112.xposed.util

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
