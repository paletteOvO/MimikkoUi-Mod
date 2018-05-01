package me.manhong2112.mimikkouimod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.xposed.MimikkoUI
import org.jetbrains.anko.defaultSharedPreferences
import java.io.Serializable
import me.manhong2112.mimikkouimod.common.TypedKey as K

class ConfigReceiver : BroadcastReceiver() {
   override fun onReceive(context: Context, intent: Intent) {
      log("received loadConfig")
      K.values.forEach {
         Config.addOnChangeListener(it) { key, value: Any ->
            log("send config update")
            context.sendBroadcast(
                  Intent(Const.updateConfigAction)
                        .putExtra("Key", key.name)
                        .putExtra("Value", value as Serializable)
                        .setPackage(MimikkoUI.packageName))
            log("after send config update")
         }
      }
      when (intent.action) {
         Const.loadConfigAction -> {
            Utils.log("received loadConfigAction")
            val pref = context.defaultSharedPreferences
            Config.loadSharedPref(pref, true)
            return
         }
      }
   }
}