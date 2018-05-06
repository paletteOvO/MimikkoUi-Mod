package me.manhong2112.mimikkouimod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.log
import org.jetbrains.anko.defaultSharedPreferences
import me.manhong2112.mimikkouimod.common.TypedKey as K

class ConfigReceiver : BroadcastReceiver() {
   override fun onReceive(context: Context, intent: Intent) {
      log("received loadConfig")
      when (intent.action) {
         Const.loadConfigAction -> {
            Utils.log("received loadConfigAction")
            val pref = context.defaultSharedPreferences
            Config.bindSharedPref(pref)
            if (intent.hasExtra("Key")) {
               Config.updateConfig(context, K.valueOf(intent.getStringExtra(Const.valueExtraName)))
            } else {
               K.values.forEach { key ->
                  Config.updateConfig(context, key)
                  log("after send config")
               }
            }
            return
         }
      }
   }
}
