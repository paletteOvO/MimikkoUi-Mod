package me.manhong2112.mimikkouimod.setting

import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.layout.PreferenceLayout
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.seekBarPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment

class DrawerSettingFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         switchPreference(R.string.pref_drawer_blur_background, key = Config.Key.DrawerBlurBackground)
         switchPreference(R.string.pref_drawer_blur_background, key = Config.Key.DrawerBlurBackground)
         seekBarPreference<Int>(R.string.pref_drawer_blur_background_radius, R.string.pref_drawer_blur_background_radius_num_format, Config.Key.DrawerBlurBackgroundBlurRadius, max = 999)
         switchPreference(R.string.pref_drawer_darken_background, key = Config.Key.DrawerDarkBackground)

         seekBarPreference<Int>(R.string.pref_drawer_column_size, R.string.pref_drawer_column_size_num_format, Config.Key.DrawerColumnSize, min = 1, max = 10)

         switchPreference(R.string.pref_drawer_draw_under_status_bar, key = Config.Key.DrawerDrawUnderStatusBar)
      }
//         switchPreference("DrawerBatSwipeToSearch", key = Config.Key.DrawerBatSwipeToSearch)
   }
}
