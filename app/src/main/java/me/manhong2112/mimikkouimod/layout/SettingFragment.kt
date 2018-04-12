package me.manhong2112.mimikkouimod.layout

import android.app.Activity
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
   private val prefLayout: ScrollView by lazy {
      init(this.activity!!)
      prefLayoutFuture!!.get()
   }
   private var prefLayoutFuture: Future<ScrollView>? = null
   override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
      with(this.activity!!) {
         log("onCreateView")
         return prefLayout
      }
   }

   fun init(ctx: Activity): SettingFragment {
      if (prefLayoutFuture === null) {
         prefLayoutFuture = doAsyncResult {
            log("init ${this.weakRef.get()?.javaClass?.name}")
            val v = ctx.UI { preferenceLayout { createView(this@preferenceLayout) } }.view as ScrollView
            log("init-ed ${this.weakRef.get()?.javaClass?.name}")
            v
         }
      }
      return this
   }

   fun open(ctx: AppCompatActivity, firstPage: Boolean = false) {
      val ft = ctx.supportFragmentManager.beginTransaction()
      if (firstPage) {
         ft.replace(android.R.id.content, this, null)
      } else {
         ft.setCustomAnimations(R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom, R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom)
         ft.replace(android.R.id.content, this, null)
         ft.addToBackStack(null)
      }
      ft.commit()
   }
   abstract fun createView(layout: PreferenceLayout)
}


