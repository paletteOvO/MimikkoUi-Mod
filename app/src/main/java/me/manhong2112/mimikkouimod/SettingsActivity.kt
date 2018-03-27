package me.manhong2112.mimikkouimod

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.preferenceLayout
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.preferencePage
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.seekBarPreference
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.selectorPreference
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.xposed.IconProvider
import org.jetbrains.anko.UI
import org.jetbrains.anko.defaultSharedPreferences
import java.io.Serializable

class SettingsActivity : AppCompatActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      Config.Key.values().forEach { key ->
         Config.setOnChangeListener(key) { key, value: Any ->
            val intent = Intent(Const.configUpdateAction)
            intent.putExtra("Key", key.name)
            intent.putExtra("Value", value as Serializable)
            sendBroadcast(intent)
         }
      }
      val intent = intent
      when (intent.action) {
         Const.loadConfigAction -> {
            log("received loadConfigAction")
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            Config.loadSharedPref(pref, true)
            finish()
            return
         }
      }
      Config.loadSharedPref(defaultSharedPreferences)
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
            log(fm.backStackEntryCount.toString())
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


   companion object {
      private fun <T> Preference.bind(callback: (Preference, T) -> Boolean) {
         this.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, value ->
            @Suppress("UNCHECKED_CAST")
            return@OnPreferenceChangeListener callback(preference, value as T)
         }
      }
   }
}

abstract class SettingFragment : Fragment() {
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return with(this.activity!!) {
         UI {
            preferenceLayout {
               createView(this@preferenceLayout)
            }
         }.view
      }
   }

   abstract fun createView(layout: PreferenceLayout)
}

class DrawerSettingFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         switchPreference(R.string.pref_drawer_blur_background, key = Config.Key.DrawerBlurBackground)
         seekBarPreference(R.string.pref_drawer_blur_background_radius, R.string.pref_drawer_blur_background_radius_num_format, Config.Key.DrawerBlurBackgroundBlurRadius, max = 999)
         switchPreference(R.string.pref_drawer_darken_background, key = Config.Key.DrawerDarkBackground)

         seekBarPreference(R.string.pref_drawer_column_size, R.string.pref_drawer_column_size_num_format, Config.Key.DrawerColumnSize, min = 1, max = 10)
      }
   }
}

class GeneralSettingFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         switchPreference(R.string.pref_general_transparent_status_bar, key = Config.Key.GeneralTransparentStatusBar)
         switchPreference(R.string.pref_general_dark_status_bar_icon, key = Config.Key.GeneralDarkStatusBarIcon)
         selectorPreference(R.string.pref_general_icon_pack, key = Config.Key.GeneralIconPack, items = IconProvider.getAllIconPack(context))
      }
   }
}

class DockSettingFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         switchPreference(R.string.pref_dock_swipe_up_gesture, key = Config.Key.DockSwipeToDrawer)
      }
   }
}