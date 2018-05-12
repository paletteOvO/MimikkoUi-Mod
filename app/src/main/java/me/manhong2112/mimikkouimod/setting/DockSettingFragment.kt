package me.manhong2112.mimikkouimod.setting

import android.view.ViewGroup
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment
import me.manhong2112.mimikkouimod.common.TypedKey as K

class DockSettingFragment : SettingFragment() {
   override fun createView(layout: ViewGroup) {
      with(layout) {
         //         preferenceGroup {
//            preferenceHeader("Drawer button gesture")
//            val displayList = listOf(
//                  "Open drawer",
//                  "Exec"
//            )
//            val onClick = { key: K<String> ->
//               { _: DialogInterface, index: Int ->
//                  when (index) {
//                     0 -> Config[key] = "OpenDrawer"
//                     1 -> context.editTextAlert("Exec") {
//
//                     }
//                  }
//               }
//            }
//            selectorPreference("Swipe Up", display = displayList, onClick = onClick(K.NeKoMiMiSwipeUpGesture))
//            selectorPreference("Swipe Down", display = displayList, onClick = onClick(K.NeKoMiMiSwipeDownGesture))
//            selectorPreference("Swipe Left", display = displayList, onClick = onClick(K.NeKoMiMiSwipeLeftGesture))
//            selectorPreference("Swipe Right", display = displayList, onClick = onClick(K.NeKoMiMiSwipeRightGesture))
//            selectorPreference("Click", display = displayList, onClick = onClick(K.NeKoMiMiClickGesture))
//            selectorPreference("Double Click", display = displayList, onClick = onClick(K.NeKoMiMiDoubleClickGesture))
//            selectorPreference("Long Click", display = displayList, onClick = onClick(K.NeKoMiMiLongClickGesture))

//         }

         switchPreference(R.string.pref_dock_swipe_up_gesture, key = K.DockSwipeToDrawer)
      }
   }
}