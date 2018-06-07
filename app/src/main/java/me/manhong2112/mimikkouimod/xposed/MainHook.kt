package me.manhong2112.mimikkouimod.xposed

import android.app.Activity
import android.app.Application
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
      injectId(lpparam)
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
      // ServantSetting().onLoad(lpparam.classLoader, lpparam)
   }

   private fun injectId(lpparam: XC_LoadPackage.LoadPackageParam) {
      XposedHelpers.findClass(MimikkoUI.appClsName, lpparam.classLoader)
            .findMethod("onCreate")
            .hook(priority = 99) {
               val app = it.thisObject as Application
               val res = app.resources

               MimikkoUI.id.dock_layout = res.getIdentifier("dock_layout", "id", MimikkoUI.packageName)
               MimikkoUI.id.drawer_layout = res.getIdentifier("drawer_layout", "id", MimikkoUI.packageName)
               MimikkoUI.id.workspace = res.getIdentifier("workspace", "id", MimikkoUI.packageName)
               MimikkoUI.id.drawerButton = res.getIdentifier("drawerButton", "id", MimikkoUI.packageName)
               MimikkoUI.id.bubble = res.getIdentifier("bubble", "id", MimikkoUI.packageName)
               MimikkoUI.id.app_settings = res.getIdentifier("app_settings", "id", MimikkoUI.packageName)
               MimikkoUI.id.bat_bar = res.getIdentifier("bat_bar", "id", MimikkoUI.packageName)
               MimikkoUI.id.bat = res.getIdentifier("bat", "id", MimikkoUI.packageName)
               MimikkoUI.id.bat_wrap = res.getIdentifier("bat_wrap", "id", MimikkoUI.packageName)

               MimikkoUI.drawable.ic_button_drawer = res.getIdentifier("ic_button_drawer", "drawable", MimikkoUI.packageName)

               MimikkoUI.dimen.app_icon_size = res.getIdentifier("app_icon_size", "dimen", MimikkoUI.packageName)
            }

   }
}