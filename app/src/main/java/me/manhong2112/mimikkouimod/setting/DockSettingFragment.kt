package me.manhong2112.mimikkouimod.setting

import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.layout.PreferenceLayout
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment

class DockSettingFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         switchPreference(R.string.pref_dock_swipe_up_gesture, key = Config.Key.DockSwipeToDrawer)
      }
   }
}