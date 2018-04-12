package me.manhong2112.mimikkouimod.layout

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewManager
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.custom.ankoView

class PreferenceLayout(private val ctx: Context) : _LinearLayout(ctx) {
   init {
      id = View.generateViewId()
      orientation = VERTICAL
      lparams(matchParent, matchParent)
      backgroundColor = Color.WHITE
      setOnTouchListener { _, _ -> true }
      Config.bindSharedPref(ctx.defaultSharedPreferences)
   }

   companion object {
      inline fun ViewManager.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = with(scrollView()) {
         isFillViewport = true
         id = View.generateViewId()
         ankoView({ PreferenceLayout(it) }, 0, init)
      }

      inline fun Activity.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = with(scrollView()) {
         isFillViewport = true
         id = View.generateViewId()
         ankoView({ PreferenceLayout(it) }, 0, init)
      }

      inline fun Context.preferenceLayout(init: PreferenceLayout.() -> Unit = {}) = with(scrollView()) {
         isFillViewport = true
         id = View.generateViewId()
         ankoView({ PreferenceLayout(it) }, 0, init)
      }

      fun PreferenceLayout.preferencePage(page: SettingFragment, nameRes: Int, summaryRes: Int = 0, icon: Drawable? = null) =
            preferencePage(page, ctx.getString(nameRes), if (summaryRes == 0) null else ctx.getString(summaryRes), icon)

      fun PreferenceLayout.preferencePage(page: SettingFragment, name: String, summary: String? = null, icon: Drawable? = null) =
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
               if (summary !== null) {
                  verticalLayout {
                     id = View.generateViewId()
                     textView {
                        id = View.generateViewId()
                        textSize = sp(6.5f).toFloat()
                        text = name
                     }
                     textView {
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
               } else {
                  textView {
                     id = View.generateViewId()
                     textSize = sp(6.5f).toFloat()
                     text = name
                  }.lparams {
                     setPadding(0, dip(6), 0, dip(6))
                     rightOf(iconImageView)
                     centerInParent()
                  }
               }
               setOnClickListener {
                  page.open(ctx as AppCompatActivity)
               }
            }.lparams {
               width = matchParent
               height = dip(Const.prefItemHeight)
            }

      fun PreferenceLayout.switchPreference(nameRes: Int, summaryRes: Int = 0, key: Config.Key, init: Switch.() -> Unit = {}) =
            switchPreference(ctx.getString(nameRes), if (summaryRes == 0) null else ctx.getString(summaryRes), key, init)

      fun PreferenceLayout.switchPreference(name: String, summary: String? = null, key: Config.Key, init: Switch.() -> Unit = {}) {
         relativeLayout {
            id = View.generateViewId()
            backgroundDrawable = getSelectedItemDrawable(ctx)
            isClickable = true
            if (summary !== null) {
               verticalLayout {
                  textView {
                     text = name
                  }
                  textView {
                     text = summary
                  }
               }.lparams {
                  padding = dip(12)
                  gravity = Gravity.CENTER_VERTICAL
                  centerInParent()
                  alignParentLeft()
               }
            } else {
               textView {
                  text = name
               }.lparams {
                  padding = dip(12)
                  centerInParent()
                  alignParentLeft()
               }
            }
            val s = switch {
               ctx.runOnUiThread {
                  isChecked = Config[key]
               }
               isClickable = false
            }.lparams {
               height = dip(Const.prefItemHeight)
               centerInParent()
               alignParentRight()
            }
            setOnClickListener {
               s.toggle()
               Config[key] = s.isChecked
            }
         }.lparams {
            width = matchParent
            height = dip(Const.prefItemHeight)
         }
      }

      fun <T : Number> PreferenceLayout.seekBarPreference(nameRes: Int, numFormatRes: Int = 0,
                                                          key: Config.Key,
                                                          min: Int = 0, max: Int = 100, step: Int = 1,
                                                          displayParse: (T) -> Int = { it.toInt() },
                                                          valueParse: (Int) -> T = { it as T },
                                                          init: SeekBar.() -> Unit = {}) {
         return seekBarPreference(ctx.getString(nameRes), if (numFormatRes == 0) null else ctx.getString(numFormatRes), key, min, max, step, displayParse, valueParse, init)
      }

      fun <T : Number> PreferenceLayout.seekBarPreference(name: String, numFormat: String? = null,
                                                          key: Config.Key,
                                                          min: Int = 0, max: Int = 100, step: Int = 1,
                                                          displayParse: (T) -> Int = { it.toInt() },
                                                          valueParse: (Int) -> T = { it as T },
                                                          init: SeekBar.() -> Unit = {}) {
         relativeLayout {
            setPadding(dip(12), 0, dip(12), 0)
            backgroundDrawable = getSelectedItemDrawable(ctx)
            isClickable = true
            id = View.generateViewId()
            val title = textView {
               id = View.generateViewId()
               text = name
               gravity = Gravity.CENTER_VERTICAL
            }.lparams {
               width = matchParent
               height = dip(Const.prefItemHeight) / 2
               alignParentLeft()
               alignParentTop()
            }
            lateinit var numView: TextView
            val defaultValue = displayParse(Config[key]) - min
            numFormat?.run {
               numView = TextView(ctx)
               numView.text = numFormat.format(Config.get<T>(key))
               numView.textSize = sp(5).toFloat()
               numView.gravity = Gravity.END or Gravity.CENTER_VERTICAL
               numView.id = View.generateViewId()
               numView.lparams {
                  below(title)
                  alignParentLeft()
                  height = dip(Const.prefItemHeight) / 2
                  width = dip(36)
               }
               addView(numView)
            }
            seekBar {
               setMax(max - min)
               incrementProgressBy(step)
               progress = defaultValue
               setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                  override fun onProgressChanged(p0: SeekBar, value: Int, p2: Boolean) {
                     numFormat?.run {
                        numView.text = numFormat.format(valueParse(value + min))
                     }
                  }

                  override fun onStartTrackingTouch(p0: SeekBar) {
                  }

                  override fun onStopTrackingTouch(p0: SeekBar) {
                     Config[key] = valueParse(p0.progress + min)
                  }
               })
            }.lparams {
               width = matchParent
               height = dip(Const.prefItemHeight) / 2
               alignParentBottom()
               if (numFormat !== null) {
                  rightOf(numView)
               } else {
                  alignParentLeft()
               }
            }
         }.lparams {
            width = matchParent
            height = dip(Const.prefItemHeight)
         }
      }

      fun PreferenceLayout.selectorPreference(nameRes: Int, summaryRes: Int = 0, items: List<Pair<String, Any>>, key: Config.Key, init: () -> Unit = {}) {
         selectorPreference(nameRes, summaryRes, items.map { it.first }, items.map { it.second }, key, init)
      }

      fun PreferenceLayout.selectorPreference(name: String, summary: String? = null, items: List<Pair<String, Any>>, key: Config.Key, init: () -> Unit = {}) {
         selectorPreference(name, summary, items.map { it.first }, items.map { it.second }, key, init)
      }

      fun PreferenceLayout.selectorPreference(nameRes: Int, summaryRes: Int = 0, displayName: List<String>, value: List<Any>? = null, key: Config.Key, init: () -> Unit = {}) {
         selectorPreference(ctx.getString(nameRes), if (summaryRes == 0) null else ctx.getString(summaryRes), displayName, value, key, init)
      }

      fun PreferenceLayout.selectorPreference(name: String, summary: String? = null, displayName: List<String>, value: List<Any>? = null, key: Config.Key, init: () -> Unit = {}) {
         relativeLayout {
            backgroundDrawable = getSelectedItemDrawable(ctx)
            isClickable = true
            id = View.generateViewId()
            if (summary !== null) {
               textView {
                  text = name
                  gravity = Gravity.BOTTOM or Gravity.START
               }.lparams {
                  height = dip(Const.prefItemHeight) / 2
                  padding = dip(12)
                  alignParentLeft()
               }
               textView {
                  text = summary
                  gravity = Gravity.TOP or Gravity.START
               }.lparams {
                  height = dip(Const.prefItemHeight) / 2
                  padding = dip(12)
                  alignParentLeft()
               }
            } else {
               textView {
                  text = name
               }.lparams {
                  padding = dip(12)
                  centerInParent()
                  alignParentLeft()
               }
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

      fun <T> PreferenceLayout.editTextPreference(nameRes: Int, summaryRes: Int = 0, hintRes: Int = 0, key: Config.Key, displayParser: (T) -> String = { it.toString() }, valueParser: (String) -> T = { it as T }) {
         editTextPreference(ctx.getString(nameRes),
               if (summaryRes == 0) null else ctx.getString(summaryRes),
               if (hintRes == 0) null else ctx.getString(hintRes),
               key,
               displayParser,
               valueParser)
      }

      fun <T> PreferenceLayout.editTextPreference(name: String, summary: String? = null, hint: String? = null, key: Config.Key, displayParser: (T) -> String = { it.toString() }, valueParser: (String) -> T = { it as T }) {
         relativeLayout {
            id = View.generateViewId()
            backgroundDrawable = getSelectedItemDrawable(ctx)
            isClickable = true
            textView {
               text = name
               gravity = if (summary === null) {
                  Gravity.CENTER_VERTICAL
               } else {
                  Gravity.BOTTOM or Gravity.START
               }
            }.lparams {
               height = dip(Const.prefItemHeight) / 2
               padding = dip(12)
               alignParentLeft()
               if (summary === null) {
                  centerInParent()
               }
            }
            summary?.let {
               textView {
                  text = it
               }.lparams {
                  alignParentLeft()
                  height = dip(Const.prefItemHeight) / 2
                  gravity = Gravity.TOP or Gravity.START
                  padding = dip(12)
               }
            }
            setOnClickListener {
               ctx.alert {
                  customView {
                     val text = editText {
                        this.hint = hint
                        setText(displayParser(Config[key]))
                     }
                     positiveButton("OK") {
                        Config[key] = valueParser(text.text.toString()) as Any
                     }
                  }
               }.show()
            }
         }.lparams {
            width = matchParent
            height = dip(Const.prefItemHeight)
         }
      }

      fun PreferenceLayout.sortingPreference(nameRes: Int, key: Config.Key, displayList: MutableList<String>, valueList: MutableList<String>) {
         sortingPreference(name = context.getString(nameRes), key = key, displayList = displayList, valueList = valueList)
      }

      // TODO summary
      fun PreferenceLayout.sortingPreference(name: String, key: Config.Key, displayList: MutableList<String>, valueList: MutableList<String>) =
            relativeLayout {
               id = View.generateViewId()
               gravity = Gravity.CENTER_VERTICAL
               isClickable = true
               backgroundDrawable = getSelectedItemDrawable(ctx)
               textView {
                  text = name
                  gravity = Gravity.CENTER_VERTICAL
               }.lparams {
                  height = dip(Const.prefItemHeight) / 2
                  padding = dip(12)
               }
               setOnClickListener {
                  context.alert {
                     customView {
                        ankoView({ DragAndDropListView<String>(it) }, 0) {
                           targetList = displayList
                           adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, displayList)
                           divider = null
                           onItemSwapListener = { i1, i2 ->
                              valueList.swap(i1, i2)
                           }
                        }
                     }
                     positiveButton("OK") {
                        Config[key] = valueList
                     }
                  }.show()
               }
            }.lparams {
               width = matchParent
               height = dip(Const.prefItemHeight)
            }

      fun PreferenceLayout.preferenceHeader(title: String) {
         relativeLayout {
            id = View.generateViewId()
            padding = dip(8)
            val titleView = textView {
               id = android.R.id.title // why?
               text = title
            }.lparams {
               centerInParent()
            }
            view {
               backgroundColor = 0xaaaaaa.opaque
            }.lparams {
               centerVertically()
               height = dip(1.5f)
               width = wrapContent

               startOf(titleView)
               marginStart = dip(64)
               marginEnd = dip(16)
            }
            view {
               backgroundColor = 0xaaaaaa.opaque
            }.lparams {
               centerVertically()
               height = dip(1.5f)
               width = wrapContent

               endOf(titleView)
               marginEnd = dip(64)
               marginStart = dip(16)
            }
            lparams {
               width = matchParent
               height = wrapContent
            }
         }
      }

      fun PreferenceLayout.preferenceGroup(init: _LinearLayout.() -> Unit = {}) {
         cardView {
            id = View.generateViewId()
            linearLayout {
               init()
            }
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
