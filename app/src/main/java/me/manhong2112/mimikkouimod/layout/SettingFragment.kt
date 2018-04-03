package me.manhong2112.mimikkouimod.layout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferenceLayout
import org.jetbrains.anko.UI

abstract class SettingFragment : Fragment() {
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      return with(this.activity!!) {
         UI {
            preferenceLayout {
               createView(this@preferenceLayout)
            }
         }.view
      }
   }

   abstract fun createView(layout: PreferenceLayout)
}