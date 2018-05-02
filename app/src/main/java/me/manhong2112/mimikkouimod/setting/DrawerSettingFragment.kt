package me.manhong2112.mimikkouimod.setting

import android.view.ViewGroup
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.seekBarPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment
import me.manhong2112.mimikkouimod.common.TypedKey as K
class DrawerSettingFragment : SettingFragment() {
   override fun createView(layout: ViewGroup) {
      with(layout) {
         switchPreference(R.string.pref_drawer_blur_background, key = K.DrawerBlurBackground)
         seekBarPreference(R.string.pref_drawer_blur_background_radius, R.string.pref_drawer_blur_background_radius_num_format, K.DrawerBlurBackgroundBlurRadius, max = 999)
         switchPreference(R.string.pref_drawer_darken_background, key = K.DrawerDarkBackground)

         seekBarPreference(R.string.pref_drawer_column_size, R.string.pref_drawer_column_size_num_format, K.DrawerColumnSize, min = 1, max = 10)

         switchPreference(R.string.pref_drawer_draw_under_status_bar, key = K.DrawerDrawUnderStatusBar)

         switchPreference(R.string.pref_drawer_bat_swipe_to_search, key = K.DrawerBatSwipeToSearch)
      }
//         switchPreference("DrawerBatSwipeToSearch", key = K.DrawerBatSwipeToSearch)
   }
}
