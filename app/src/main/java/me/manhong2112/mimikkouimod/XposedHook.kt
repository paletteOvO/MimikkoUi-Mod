package me.manhong2112.mimikkouimod

import de.robv.android.xposed.callbacks.XC_LoadPackage
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import android.R.attr.classLoader
import android.app.Application
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import android.R.attr.classLoader
import android.content.Context
import de.robv.android.xposed.*
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.AccessibleObject.setAccessible
import java.util.*


class XposedHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
   fun log(msg : String) {
      println("==MMKUI==Mod==$msg")
   }
   override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

      if (lpparam.packageName != "com.mimikko.mimikkoui") return
      val cls = XposedHelpers.findClass("com.stub.StubApp", lpparam.classLoader)
      val m = XposedHelpers.findMethodExact(cls, "attachBaseContext", Context::class.java)
      m.isAccessible = true
      val cryAppField = cls.getDeclaredField("יﾞ")
      cryAppField.isAccessible = true
      hookMethod(m, object : XC_MethodHook() {
         @Throws(Throwable::class)
         override fun beforeHookedMethod(param: MethodHookParam) {
            log("beforeHookedMethod attachBaseContext")
         }
         @Throws(Throwable::class)
         override fun afterHookedMethod(param: MethodHookParam) {
            log("afterHookedMethod attachBaseContext")
            val app = cryAppField.get(param.thisObject) as Application

            val cls = XposedHelpers.findClass("com.mimikko.mimikkoui.launcher.activity.Launcher", app.classLoader)
            cls.declaredMethods.map {
               log(it.name)
            }
            cls.declaredFields.map {
               log(it.name)
            }
         }
      })
      println("==end==")
   }

   @Throws(Throwable::class)
   override fun handleInitPackageResources(resparam: InitPackageResourcesParam) {
      if (resparam.packageName != "com.mimikko.mimikkoui") return

   }
}