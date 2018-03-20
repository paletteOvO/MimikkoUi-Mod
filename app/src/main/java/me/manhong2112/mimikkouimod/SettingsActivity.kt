package me.manhong2112.mimikkouimod


import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.view.MenuItem
import org.jetbrains.anko.act

class SettingsActivity : AppCompatPreferenceActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      val pref = PreferenceManager.getDefaultSharedPreferences(this)

      setupActionBar()
   }

   private fun setupActionBar() {
      supportActionBar?.setDisplayHomeAsUpEnabled(true)
   }

   override fun onIsMultiPane(): Boolean {
      return isXLargeTablet(this)
   }

   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
      loadHeadersFromResource(R.xml.pref_headers, target)
   }

   override fun isValidFragment(fragmentName: String): Boolean {
      return when (fragmentName) {
         PreferenceFragment::class.java.name,
         GeneralPreferenceFragment::class.java.name -> true
         else -> false
      }
   }

   @TargetApi(Build.VERSION_CODES.HONEYCOMB)
   class GeneralPreferenceFragment : PreferenceFragment() {
      override fun onCreate(savedInstanceState: Bundle?) {
         super.onCreate(savedInstanceState)
         addPreferencesFromResource(R.xml.pref_general)
         setHasOptionsMenu(true)

         bindPreference<Boolean>(Config.ConfigType.Drawer, Config.Drawer.DrawerBlurBackground)
      }

      override fun onOptionsItemSelected(item: MenuItem): Boolean {
         if (item.itemId == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
         }
         return super.onOptionsItemSelected(item)
      }

      private fun <T>PreferenceFragment.bindPreference(type: Config.ConfigType, key: Enum<*>) {
         when(type) {
            Config.ConfigType.Drawer ->
               findPreference(key.name).bind<T> {
                  pref, value ->
                  val cfg = Config(type)
                  cfg.set(key, value as Any)
                  val intent = Intent(Const.updateDrawerAction)
                  intent.putExtra("Config", cfg)
                  act.sendBroadcast(intent)
                  true
               }
            Config.ConfigType.Dock ->
               findPreference(key.name).bind<T> { preference, value ->
                  val cfg = Config(type)
                  cfg.set(key, value as Any)
                  val intent = Intent(Const.updateDockAction)
                  intent.putExtra("Config", cfg)
                  act.sendBroadcast(intent)
                  true
               }
         }
      }
   }

   companion object {
      private fun <T> Preference.bind(callback: (Preference, T) -> Boolean) {
         this.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, value ->
            return@OnPreferenceChangeListener callback(preference, value as T)
         }
      }

      private fun isXLargeTablet(context: Context): Boolean {
         return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
      }

   }
}
