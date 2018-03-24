package me.manhong2112.mimikkouimod

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.preferenceLayout
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.preferencePage
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.sendConfig
import me.manhong2112.mimikkouimod.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.Utils.log
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.ctx


//
//class SettingsActivity : AppCompatPreferenceActivity() {
//
//   class GeneralPreferenceFragment : PreferenceFragment() {
//      private val updater by lazy {
//         makeUpdater(this.activity, Config.ConfigType.Drawer, Const.updateDrawerAction)
//      }
//
//      override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         addPreferencesFromResource(R.xml.pref_general)
//         setHasOptionsMenu(true)
//
//         bindUpdater(act, updater)
//      }
//
//      override fun onOptionsItemSelected(item: MenuItem): Boolean {
//         if (item.itemId == android.R.id.home) {
//            startActivity(Intent(activity, SettingsActivity::class.java))
//            return true
//         }
//         return super.onOptionsItemSelected(item)
//      }
//   }
//
//   override fun onCreate(savedInstanceState: Bundle?) {
//      super.onCreate(savedInstanceState)
//      PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)
//      val intent = intent
//      when (intent.action) {
//         Const.loadConfigAction -> {
//            log("received loadConfigAction")
//            val pref = PreferenceManager.getDefaultSharedPreferences(this)
//            sendConfig(Const.updateDrawerAction, Config.fromSharedPref(Config.ConfigType.Drawer, pref))
//            sendConfig(Const.updateDockAction, Config.fromSharedPref(Config.ConfigType.Dock, pref))
//            finish()
//         }
//         else -> {
//            supportActionBar?.setDisplayHomeAsUpEnabled(true)
//         }
//      }
//   }
//
//   override fun onIsMultiPane(): Boolean {
//      return isXLargeTablet(this)
//   }
//
//   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
//   override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
//      loadHeadersFromResource(R.xml.pref_headers, target)
//   }
//
//   override fun isValidFragment(fragmentName: String): Boolean {
//      return when (fragmentName) {
//         PreferenceFragment::class.java.name,
//         GeneralPreferenceFragment::class.java.name -> true
//         else -> false
//      }
//   }
//
//
//

//}

class SettingsActivity : AppCompatActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      PreferenceManager.setDefaultValues(this, R.xml.pref_general, false)
      val intent = intent
      when (intent.action) {
         Const.loadConfigAction -> {
            log("received loadConfigAction")
            val pref = PreferenceManager.getDefaultSharedPreferences(this)
            sendConfig(Config.fromSharedPref(Config.ConfigType.Drawer, pref))
            sendConfig(Config.fromSharedPref(Config.ConfigType.Dock, pref))
            finish()
         }
         else -> {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
         }
      }
      preferenceLayout {
         preferencePage(SettingFragment(), "name", "summary", ContextCompat.getDrawable(ctx, R.drawable.abc_btn_radio_material))
         preferencePage(SettingFragment2(), "name2", "summary2", ContextCompat.getDrawable(ctx, R.drawable.abc_btn_radio_material))
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

class SettingFragment : Fragment() {
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return with(this.ctx) {
         UI {
            preferenceLayout {
               switchPreference("DrawerBlurBackground", null, Config.ConfigType.Drawer, Config.Drawer.DrawerBlurBackground)
            }
         }.view
      }
   }
}

class SettingFragment2 : Fragment() {
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return with(this.ctx) {
         UI {
            preferenceLayout {
               textView {
                  backgroundColor = Color.RED
                  height = dip(64)
                  width = matchParent
               }
            }
         }.view
      }
   }
}

class SettingFragment3 : Fragment() {
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return with(this.ctx) {
         UI {
            preferenceLayout {
            }
         }.view
      }
   }
}
