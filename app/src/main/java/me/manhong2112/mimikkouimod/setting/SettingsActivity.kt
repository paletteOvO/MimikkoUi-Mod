package me.manhong2112.mimikkouimod.setting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferenceLayout
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferencePage
import org.jetbrains.anko.ctx
import org.jetbrains.anko.defaultSharedPreferences
import me.manhong2112.mimikkouimod.common.TypedKey as K

class SettingsActivity : AppCompatActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      K.values.forEach {
         Config.addOnChangeListener(it) { key, value: Any ->
            log("send config $key -> $value")
            Config.updateConfig(ctx, key)
         }
      }
      Config.bindSharedPref(defaultSharedPreferences)
      supportActionBar?.setDisplayHomeAsUpEnabled(true)

      preferenceLayout {
         preferencePage(GeneralSettingFragment(), R.string.pref_page_general)
         preferencePage(DrawerSettingFragment(), R.string.pref_page_drawer)
         preferencePage(DockSettingFragment(), R.string.pref_page_dock)
      }
   }

   override fun onOptionsItemSelected(item: MenuItem): Boolean {
      val fm = supportFragmentManager
      when (item.itemId) {
         android.R.id.home -> {
            if (fm.backStackEntryCount > 0) {
               fm.popBackStackImmediate()
            } else {
               finish()
            }
            return true
         }
      }
      return super.onOptionsItemSelected(item)
   }
}
