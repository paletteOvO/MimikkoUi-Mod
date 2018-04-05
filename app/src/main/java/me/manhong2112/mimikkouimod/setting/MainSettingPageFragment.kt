package me.manhong2112.mimikkouimod.setting

import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.layout.PreferenceLayout
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferencePage
import me.manhong2112.mimikkouimod.layout.SettingFragment

class MainSettingPageFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         preferencePage(GeneralSettingFragment(), R.string.pref_page_general)
         preferencePage(DrawerSettingFragment(), R.string.pref_page_drawer)
         preferencePage(DockSettingFragment(), R.string.pref_page_dock)
      }
   }
}