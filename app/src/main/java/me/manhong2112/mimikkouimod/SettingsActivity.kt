package me.manhong2112.mimikkouimod


import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.support.v7.app.ActionBar
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.preference.RingtonePreference
import android.text.TextUtils
import android.view.MenuItem
import me.manhong2112.mimikkouimod.R

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
      println(fragmentName)
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

         // bindPreferenceSummaryToValue(findPreference("drawerBlur"))
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
      private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener {
         preference, value ->
         val stringValue = value.toString()

         if (preference is ListPreference) {
            val index = preference.findIndexOfValue(stringValue)
            preference.setSummary(
                  if (index >= 0)
                     preference.entries[index]
                  else
                     null)
         } else {
            preference.summary = stringValue
         }
         true
      }

      private fun isXLargeTablet(context: Context): Boolean {
         return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
      }

      private fun bindPreferenceSummaryToValue(preference: Preference) {
         preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

         sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
               PreferenceManager
                     .getDefaultSharedPreferences(preference.context)
                     .getString(preference.key, ""))
      }
   }
}
