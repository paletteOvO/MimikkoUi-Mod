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
import me.manhong2112.mimikkouimod.BuildConfig
import me.manhong2112.mimikkouimod.ConfigReceiver
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.ReflectionUtils.findMethod
import me.manhong2112.mimikkouimod.common.ReflectionUtils.hook
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.TypedKey as K

class MainHook : IXposedHookLoadPackage {
   private val configUpdateReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            val key = intent.getStringExtra("Key")
            val value = intent.getSerializableExtra("Value")
            Utils.log("receive config $key -> $value")
            Config[K.valueOf(key)] = value as Any
         }
      }
   }
   private val servantPrefReceiver by lazy {
      servantSetting.Receiver()
   }
   private val generalHook by lazy {
      GeneralHook()
   }
   private val drawerHook by lazy {
      DrawerHook()
   }
   private val servantSetting by lazy {
      ServantSetting()
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
         app.registerReceiver(servantPrefReceiver, IntentFilter(Const.updateServantPrefAction))
         loadConfig(app)
      }

      launcherClass.findMethod("onDestroy").hook { param ->
         val app = (param.thisObject as Activity).application
         app.unregisterReceiver(configUpdateReceiver)
         app.unregisterReceiver(servantPrefReceiver)
      }
      generalHook.onLoad(lpparam.classLoader, lpparam)
      drawerHook.onLoad(lpparam.classLoader, lpparam)
      servantSetting.onLoad(lpparam.classLoader, lpparam)
   }


   private fun loadConfig(app: Context) {
      Utils.log("send loadConfig")
      val intent = Intent(Const.loadConfigAction)
      intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
      intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
      intent.setClassName(BuildConfig.APPLICATION_ID, ConfigReceiver::class.java.name)
      app.sendBroadcast(intent)
   }
}