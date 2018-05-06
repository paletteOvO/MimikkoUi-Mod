package me.manhong2112.mimikkouimod.xposed

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.Const.keyExtraName
import me.manhong2112.mimikkouimod.common.Const.valueExtraName
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.common.findMethod
import me.manhong2112.mimikkouimod.common.hook
import org.jetbrains.anko.defaultSharedPreferences

class ServantSetting {
   enum class SettingName(name: String) {
      NoticeEnable("key_servant_notice"),
      SystemNotice("key_servant_system_notice"),
      RandomNotice("key_servant_random_notice"),
      DressUpNotice("key_servant_dressup_notice"),
      AppNotice("key_servant_app_notice"),

      ServantEnable("key_servant_enable"),
      ServantMute("key_servant_mute"),
      ServantTouchable("key_servant_touchable"),
      ServantLocked("key_servant_locked"),
      // ServantPosReset("key_servant_pos_reset"),
      ;

      val prefName: String = name
   }

   private val servantSettingCls by lazy {
      XposedHelpers.findClass(MimikkoUI.servantSettingClsName, classLoader)
   }
   private lateinit var app: Application
   private lateinit var classLoader: ClassLoader
   fun onLoad(classLoader: ClassLoader, lpparam: XC_LoadPackage.LoadPackageParam) {
      this.classLoader = classLoader
      val launcherClass = XposedHelpers.findClass(MimikkoUI.launcherClsName, lpparam.classLoader)
      launcherClass.findMethod("onCreate", Bundle::class.java).hook(0) { param ->
         app = (param.thisObject as Activity).application
         app.registerReceiver(receiver, IntentFilter(Const.updateServantPrefAction))
      }

      launcherClass.findMethod("onDestroy").hook { param ->
         val app = (param.thisObject as Activity).application
         app.unregisterReceiver(receiver)
      }
   }

   fun handle(key: SettingName, value: Any) {
      app.defaultSharedPreferences
            .edit()
            .also {
               log("putBoolean ${key.prefName} $value")
               it.putBoolean(key.prefName, value as Boolean)
            }
            .apply()
   }

   private val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
         if (intent.action == Const.updateServantPrefAction) {
            handle(SettingName.valueOf(intent.getStringExtra(keyExtraName)), intent.getSerializableExtra(valueExtraName))
         }
      }
   }
}