package me.manhong2112.mimikkouimod.layout

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferenceLayout
import org.jetbrains.anko.UI


abstract class SettingFragment : Fragment() {
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
      with(this.activity!!) {
         val v = UI {
            preferenceLayout {
               createView(this@preferenceLayout)
            }
         }.view
         return v
      }
   }
   fun init(ctx: AppCompatActivity) {
      val ft = ctx.supportFragmentManager.beginTransaction()
      ft.setCustomAnimations(R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom, R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom)
      ft.replace(android.R.id.content, this, null)
      ft.commitAllowingStateLoss()
   }

   fun open(ctx: AppCompatActivity) {
      val ft = ctx.supportFragmentManager.beginTransaction()
      ft.setCustomAnimations(R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom, R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom)
      ft.add(android.R.id.content, this, null)
      ft.addToBackStack(null)
      ft.commitAllowingStateLoss()
   }

   abstract fun createView(layout: PreferenceLayout)
}


