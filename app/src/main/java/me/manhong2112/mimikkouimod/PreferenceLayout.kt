package me.manhong2112.mimikkouimod

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.ankoView

class PreferenceLayout(private val ctx: Context) : _LinearLayout(ctx) {
   init {
      orientation = VERTICAL
      lparams(matchParent, matchParent)
      backgroundColor = Color.WHITE
      setOnTouchListener { _, _ -> true }
      Config.bindSharedPref(ctx.defaultSharedPreferences)
   }

   companion object {
      inline fun ViewManager.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = ankoView({ PreferenceLayout(it) }, 0, init)
      inline fun Activity.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = ankoView({ PreferenceLayout(it) }, 0, init)
      inline fun Context.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = ankoView({ PreferenceLayout(it) }, 0, init)

      fun PreferenceLayout.preferencePage(page: Fragment, name: String, summary: String? = null, icon: Drawable? = null) =
            relativeLayout {
               id = View.generateViewId()
               backgroundDrawable = getSelectedItemDrawable(ctx)
               isClickable = true
               val iconImageView = imageView {
                  id = View.generateViewId()
                  padding = dip(12)
                  image = icon
               }.lparams {
                  width = dip(Const.prefIconWidth) + dip(12)
                  height = dip(Const.prefIconHeight)
                  alignParentLeft()
                  centerInParent()
               }
               verticalLayout {
                  id = View.generateViewId()
                  textView {
                     id = View.generateViewId()
                     textSize = sp(6.5f).toFloat()
                     text = name
                  }
                  if (summary !== null) textView {
                     id = View.generateViewId()
                     textSize = sp(6).toFloat()
                     text = summary
                     textColor = Color.DKGRAY
                  }
               }.lparams {
                  setPadding(0, dip(6), 0, dip(6))
                  rightOf(iconImageView)
                  centerInParent()
                  gravity = Gravity.CENTER_VERTICAL
               }
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

      fun PreferenceLayout.switchPreference(name: String, summary: String? = null, key: Config.Key, init: Switch.() -> Unit = {}) {
         relativeLayout {
            backgroundDrawable = getSelectedItemDrawable(ctx)
            isClickable = true
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
            }
            val s = switch {
               isChecked = Config[key]
               isClickable = false
            }.lparams {
               width = wrapContent
               height = dip(Const.prefItemHeight)
               centerInParent()
               alignParentRight()
            }
            setOnClickListener {
               s.isChecked = !s.isChecked
               Config[key] = s.isChecked
            }
         }.lparams {
            width = matchParent
            height = dip(Const.prefItemHeight)
         }
      }

      fun PreferenceLayout.seekBarPreference(name: String, numFormat: String? = null,
                                             key: Config.Key,
                                             min: Int = 0, max: Int = 100, step: Int = 1,
                                             init: SeekBar.() -> Unit = {}) {
         linearLayout {
            setPadding(dip(12), 0, dip(12), 0)
            val defaultValue = Config.get<Int>(key) - min
            orientation = VERTICAL
            backgroundDrawable = getSelectedItemDrawable(ctx)
            isClickable = true
            textView {
               text = name
               gravity = Gravity.CENTER_VERTICAL
            }.lparams {
               width = matchParent
               weight = 0.5f
            }
            linearLayout {
               gravity = Gravity.CENTER_VERTICAL
               val numView = TextView(ctx)
               if (numFormat !== null) {
                  addView(numView, dip(28), wrapContent)
                  numView.text = numFormat.format(defaultValue + min)
                  numView.textSize = sp(5).toFloat()
                  numView.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL
               }
               seekBar {
                  setMax(max)
                  incrementProgressBy(step)
                  progress = defaultValue
                  setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                     override fun onProgressChanged(p0: SeekBar, value: Int, p2: Boolean) {
                        if (numFormat !== null) {
                           numView.text = numFormat.format(value + min)
                        }
                     }

                     override fun onStartTrackingTouch(p0: SeekBar) {
                     }

                     override fun onStopTrackingTouch(p0: SeekBar) {
                        Config[key] = p0.progress + min
                     }
                  })
               }.lparams {
                  width = matchParent
               }
            }.lparams {
               width = matchParent
               weight = 0.5f
            }
         }.lparams {
            width = matchParent
            height = dip(Const.prefItemHeight)
         }
      }

      fun PreferenceLayout.selectorPreference(name: String, summary: String? = null, items: List<Pair<String, String>>, key: Config.Key, init: () -> Unit = {}) {
         selectorPreference(name, summary, items.map { it.first }, items.map { it.second }, key, init)
      }

      fun PreferenceLayout.selectorPreference(name: String, summary: String? = null, displayName: List<String>, value: List<String>? = null, key: Config.Key, init: () -> Unit = {}) {
         relativeLayout {
            backgroundDrawable = getSelectedItemDrawable(ctx)
            isClickable = true
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
            }
            setOnClickListener {
               ctx.selector(name, displayName) { dialog, index ->
                  Config[key] = (value ?: displayName)[index]
               }
            }
         }.lparams {
            width = matchParent
            height = dip(Const.prefItemHeight)
         }

      }

      private fun getSelectedItemDrawable(ctx: Context): Drawable {
         val ta = ctx.obtainStyledAttributes(intArrayOf(R.attr.selectableItemBackground))
         val selectedItemDrawable = ta.getDrawable(0)
         ta.recycle()
         return selectedItemDrawable
      }
   }
}
