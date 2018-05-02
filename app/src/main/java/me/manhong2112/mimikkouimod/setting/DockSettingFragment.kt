package me.manhong2112.mimikkouimod.setting

import android.view.ViewGroup
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment
import me.manhong2112.mimikkouimod.common.TypedKey as K

class DockSettingFragment : SettingFragment() {
   override fun createView(layout: ViewGroup) {
      with(layout) {
         switchPreference(R.string.pref_dock_swipe_up_gesture, key = K.DockSwipeToDrawer)
      }
   }
}