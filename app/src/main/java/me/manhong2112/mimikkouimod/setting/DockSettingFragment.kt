package me.manhong2112.mimikkouimod.setting

import android.content.DialogInterface
import android.view.ViewGroup
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferenceGroup
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferenceHeader
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.selectorPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment
import me.manhong2112.mimikkouimod.layout.editTextAlert
import org.json.JSONObject
import me.manhong2112.mimikkouimod.common.TypedKey as K

typealias Neko = NekoMimiGestureAction
class DockSettingFragment : SettingFragment() {
   override fun createView(layout: ViewGroup) {
      with(layout) {
         preferenceGroup {
            preferenceHeader("Drawer button gesture")
            val displayList = listOf(
                  "No action",
                  "Open drawer",
                  "Exec"
            )
            val onClick = { key: K<String> ->
               { _: DialogInterface, index: Int ->
                  when (displayList[index]) {
                     "Exec" -> {
                        val text = Neko.fromString(Config[key]).let {
                           if (it.id == "Exec") {
                              it.arg
                           } else null
                        } ?: ""
                        context.editTextAlert("Exec", defaultText = text) {
                           Config[key] = Neko("Exec", it)
                        }
                     }
                     else ->
                        Config[key] = Neko(displayList[index].replace(" ", "_"), null)
                  }
               }
            }
            selectorPreference("Swipe Up", display = displayList, onClick = onClick(K.NeKoMiMiSwipeUpGesture))
            selectorPreference("Swipe Down", display = displayList, onClick = onClick(K.NeKoMiMiSwipeDownGesture))
            selectorPreference("Swipe Left", display = displayList, onClick = onClick(K.NeKoMiMiSwipeLeftGesture))
            selectorPreference("Swipe Right", display = displayList, onClick = onClick(K.NeKoMiMiSwipeRightGesture))
            selectorPreference("Click", display = displayList, onClick = onClick(K.NeKoMiMiClickGesture))
            selectorPreference("Double Click", display = displayList, onClick = onClick(K.NeKoMiMiDoubleClickGesture))
            selectorPreference("Long Click", display = displayList, onClick = onClick(K.NeKoMiMiLongClickGesture))
         }
      }
   }
}

val NoAction = Neko("No_action")

data class NekoMimiGestureAction(val id: String, val arg: String? = null) {
   private val cache by lazy {
      val jsonObject = JSONObject()
      jsonObject.put("id", id)
      jsonObject.put("arg", arg ?: JSONObject.NULL)
      jsonObject.toString()
   }

   override fun toString(): String {
      return cache
   }

   companion object {
      fun fromString(json: String): Neko {
         val jsonObject = JSONObject(json)
         return Neko(jsonObject["id"] as String, jsonObject["arg"] as String?)
      }
   }
}
