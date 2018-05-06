package me.manhong2112.mimikkouimod.xposed

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.common.*
import me.manhong2112.mimikkouimod.common.Config.loadConfig
import me.manhong2112.mimikkouimod.common.Const.keyExtraName
import me.manhong2112.mimikkouimod.common.Const.valueExtraName
import me.manhong2112.mimikkouimod.common.TypedKey as K

class MainHook : IXposedHookLoadPackage {
   private val configUpdateReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            val key = intent.getStringExtra(keyExtraName)
            val value = intent.getSerializableExtra(valueExtraName)
            Utils.log("received config $key -> $value")
            Config[K.valueOf(key)] = value as Any
         }
      }
   }

   override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
      if (lpparam.packageName != MimikkoUI.packageName) return
      Utils.getPackageVersion(lpparam)?.run {
         val versionName = first
         val versionCode = second
         if (versionCode != Const.supportedVersionCode || versionName != Const.supportedVersionName) {
            return
         }
      } ?: run {
         Utils.log("null version info")
         return
      }
      val launcherClass = XposedHelpers.findClass(MimikkoUI.launcherClsName, lpparam.classLoader)
      launcherClass.findMethod("onCreate", Bundle::class.java).hook(0) { param ->
         val app = (param.thisObject as Activity).application
         app.registerReceiver(configUpdateReceiver, IntentFilter(Const.updateConfigAction))
         loadConfig(app)
      }

      launcherClass.findMethod("onDestroy").hook { param ->
         val app = (param.thisObject as Activity).application
         app.unregisterReceiver(configUpdateReceiver)
      }
      GeneralHook().onLoad(lpparam.classLoader, lpparam)
      DrawerHook().onLoad(lpparam.classLoader, lpparam)
      ServantSetting().onLoad(lpparam.classLoader, lpparam)
   }
}