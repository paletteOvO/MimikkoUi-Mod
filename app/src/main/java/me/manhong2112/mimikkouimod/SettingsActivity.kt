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
import android.view.MenuItem

class SettingsActivity : AppCompatPreferenceActivity() {
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
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
      return when(fragmentName) {
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

         bindPreference<Boolean>(findPreference(Config.Drawer.DrawerBlurBackground.name)) {
            _, value ->
            val cfg = Config.getDefaultDrawerConfig()
            cfg.set(Config.Drawer.DrawerBlurBackground, value)
            this.activity.sendBroadcast(cfg.toIntent())
            true
         }
      }

      override fun onOptionsItemSelected(item: MenuItem): Boolean {
         if (item.itemId == android.R.id.home) {
            startActivity(Intent(activity, SettingsActivity::class.java))
            return true
         }
         return super.onOptionsItemSelected(item)
      }
   }

   companion object {
      private fun isXLargeTablet(context: Context): Boolean {
         return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
      }

      private fun <T>bindPreference(preference: Preference, callback: (Preference, T) -> Boolean) {
         preference.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
            preference, value ->
            return@OnPreferenceChangeListener callback(preference, value as T)
         }

      }
   }
}
