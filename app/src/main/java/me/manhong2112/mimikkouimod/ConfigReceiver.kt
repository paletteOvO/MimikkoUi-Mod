package me.manhong2112.mimikkouimod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.log
import org.jetbrains.anko.defaultSharedPreferences
import java.io.Serializable

class ConfigReceiver : BroadcastReceiver() {
   override fun onReceive(context: Context, intent: Intent) {
      log("received loadConfig")
      Config.Key.values().forEach { key ->
         Config.setOnChangeListener(key) { key, value: Any ->
            log("send config update")
            val intent = Intent(Const.configUpdateAction)
            intent.putExtra("Key", key.name)
            intent.putExtra("Value", value as Serializable)
            intent.`package` = Const.mimikkouiPackageName
            context.sendBroadcast(intent)
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