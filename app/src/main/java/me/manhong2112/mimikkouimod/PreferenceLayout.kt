package me.manhong2112.mimikkouimod

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceManager
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView

class PreferenceLayout(private val ctx: Context) : _LinearLayout(ctx) {
   private val updaters = arrayOfNulls<SharedPreferences.OnSharedPreferenceChangeListener>(Config.ConfigType.values().size)
   fun bindUpdater(type: Config.ConfigType) {
      if (updaters[type.ordinal] !== null) return
      updaters[type.ordinal] = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, name ->
         val cfg = Config.fromSharedPref(type, sharedPreferences)
         ctx.sendConfig(cfg)
      }
      PreferenceManager.getDefaultSharedPreferences(ctx).registerOnSharedPreferenceChangeListener(updaters[type.ordinal])
   }

   init {
      orientation = VERTICAL
      lparams(matchParent, matchParent)
      backgroundColor = Color.WHITE
      setOnTouchListener { _, _ -> true }
   }

   companion object {
      inline fun ViewManager.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = ankoView({ PreferenceLayout(it) }, 0, init)
      inline fun Activity.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = ankoView({ PreferenceLayout(it) }, 0, init)
      inline fun Context.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = ankoView({ PreferenceLayout(it) }, 0, init)

      fun <T> PreferenceLayout.switchPreference(name: String, summary: String?, type: Config.ConfigType, key: T, init: PreferenceLayout.() -> Unit = {}) where T : Enum<*>, T : me.manhong2112.mimikkouimod.Config.ConfigKey {
         relativeLayout {
            verticalLayout {
               textView {
                  text = name
               }
               if (summary !== null) textView {
                  text = summary
               }
            }.lparams {
               padding = dip(12)
               gravity = Gravity.CENTER_VERTICAL
               centerInParent()
               alignParentLeft()
               backgroundDrawable = getSelectedItemDrawable(ctx)
               isClickable = true
            }
            val s = switch {
               isChecked = context.defaultSharedPreferences.getBoolean(key.name, key.getDefaultValue() as Boolean)
               isClickable = false
            }.lparams {
               width = wrapContent
               height = dip(Const.prefItemHeight)
               centerInParent()
               alignParentRight()
            }
            bindUpdater(type)
            setOnClickListener {
               s.isChecked = !s.isChecked
               context.defaultSharedPreferences.edit().putBoolean(key.name, s.isChecked).apply()
            }
         }.lparams {
            width = matchParent
            height = dip(Const.prefItemHeight)
         }
      }

      fun PreferenceLayout.preferencePage(page: Fragment, name: String, summary: String, icon: Drawable?) =
            relativeLayout {
               val iconImageView = imageView {
                  id = View.generateViewId()
                  padding = dip(12)
                  maxWidth = dip(Const.prefIconWidth)
                  maxHeight = dip(Const.prefIconHeight)
                  image = icon
               }.lparams {
                  alignParentLeft()
                  centerInParent()
               }
               verticalLayout {
                  textView {
                     id = View.generateViewId()
                     textSize = sp(6.5f).toFloat()
                     text = name
                  }
                  textView {
                     id = View.generateViewId()
                     textSize = sp(6).toFloat()
                     text = summary
                  }
               }.lparams {
                  setPadding(0, dip(6), 0, dip(6))
                  rightOf(iconImageView)
                  centerInParent()
                  gravity = Gravity.CENTER_VERTICAL
               }
               backgroundDrawable = getSelectedItemDrawable(ctx)
               isClickable = true
               setOnClickListener {
                  val ft = (ctx as AppCompatActivity).supportFragmentManager.beginTransaction()
                  ft.setCustomAnimations(R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom, R.anim.fade_slide_in_bottom, R.anim.fade_slide_out_bottom)
                  ft.add(android.R.id.content, page, null)
                  ft.addToBackStack(null)
                  ft.commit()
               }
            }.lparams {
               width = matchParent
               height = dip(Const.prefItemHeight)
            }

      fun Context.sendConfig(cfg: Config) {
         val intent = Intent(cfg.type.action)
         intent.putExtra("Config", cfg)
         sendBroadcast(intent)
      }

      private fun getSelectedItemDrawable(ctx: Context): Drawable {
         val ta = ctx.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))
         val selectedItemDrawable = ta.getDrawable(0)
         ta.recycle()
         return selectedItemDrawable
      }
   }
}