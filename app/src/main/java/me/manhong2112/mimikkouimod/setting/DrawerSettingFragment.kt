package me.manhong2112.mimikkouimod.setting

import android.view.ViewGroup
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.seekBarPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment
import me.manhong2112.mimikkouimod.common.TypedKey as K

class DrawerSettingFragment : SettingFragment() {
   override fun createView(layout: ViewGroup) {
      with(layout) {
         seekBarPreference(R.string.pref_drawer_column_size, R.string.pref_drawer_column_size_num_format, K.DrawerColumnSize, min = 1, max = 10)
      }
//         switchPreference("DrawerBatSwipeToSearch", key = K.DrawerBatSwipeToSearch)
   }
}
