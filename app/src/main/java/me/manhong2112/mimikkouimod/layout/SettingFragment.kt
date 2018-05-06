package me.manhong2112.mimikkouimod.layout

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.preferenceLayout
import org.jetbrains.anko.UI
import org.jetbrains.anko.doAsyncResult
import java.util.concurrent.Future


abstract class SettingFragment : Fragment() {
   private val prefLayout: ScrollView
      get() {
         return future!!.get()
      }
   private var future: Future<ScrollView>? = null

   final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      with(this.activity!!) {
         log("onCreateView")
         return prefLayout
      }
   }

   fun init(ctx: Context) {
      if (future === null) {
         future = doAsyncResult {
            ctx.UI {
               createView(preferenceLayout {})
            }.view as ScrollView
         }
      }
   }

   fun open(ctx: AppCompatActivity, anim: Boolean = false) {
      init(ctx)
      val ft = ctx.supportFragmentManager.beginTransaction()
      if (anim) {
         ft.replace(android.R.id.content, this, "Setting")
      } else {
         ft.setCustomAnimations(R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom, R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom)
         ft.replace(android.R.id.content, this, "Setting")
         ft.addToBackStack(null)
      }
      ft.commit()
   }

   abstract fun createView(layout: ViewGroup)
}
