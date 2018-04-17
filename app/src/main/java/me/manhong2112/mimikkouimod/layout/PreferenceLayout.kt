package me.manhong2112.mimikkouimod.layout

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.*
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
      backgroundColorResource = R.color.backgroundNormal
      setOnTouchListener { _, _ -> true }
      Config.bindSharedPref(ctx.defaultSharedPreferences)
   }

   companion object {
      inline fun ViewManager.preferenceLayout(init: ViewGroup.() -> Unit = {}) = with(scrollView()) {
         isFillViewport = true
         id = View.generateViewId()
         ankoView({ PreferenceLayout(it) }, 0, init)
      }

      inline fun Activity.preferenceLayout(init: ViewGroup.() -> Unit = {}) = with(scrollView()) {
         isFillViewport = true
         id = View.generateViewId()
         ankoView({ PreferenceLayout(it) }, 0, init)
      }

      inline fun Context.preferenceLayout(init: ViewGroup.() -> Unit = {}) = with(scrollView()) {
         isFillViewport = true
         id = View.generateViewId()
         ankoView({ PreferenceLayout(it) }, 0, init)
      }

      fun ViewGroup.preferencePage(page: SettingFragment, nameRes: Int, summaryRes: Int = 0, icon: Drawable? = null) =
            preferencePage(page, context.getString(nameRes), if (summaryRes == 0) null else context.getString(summaryRes), icon)

      fun ViewGroup.preferencePage(page: SettingFragment, name: String, summary: String? = null, icon: Drawable? = null) {
         page.init(this@preferencePage.context as AppCompatActivity)
         basePreference(name, summary, icon) { _, _, _ ->
            setOnClickListener {
               page.open(this@preferencePage.context as AppCompatActivity)
            }
         }
      }

      fun ViewGroup.switchPreference(nameRes: Int, summaryRes: Int = 0, key: Config.Key, init: Switch.() -> Unit = {}) =
            switchPreference(context.getString(nameRes), if (summaryRes == 0) null else context.getString(summaryRes), key, init)

      fun ViewManager.switchPreference(name: String, summary: String? = null, key: Config.Key, init: Switch.() -> Unit = {}) {
         basePreference(name, summary) { _, _, _ ->
            val s = ankoView({
               object : Switch(it) {
                  override fun isLaidOut(): Boolean {
                     return height > 0 && width > 0
                  }
               }
            }, 0) {
               context.runOnUiThread {
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
         }
      }

      fun <T : Number> ViewGroup.seekBarPreference(nameRes: Int, numFormatRes: Int = 0,
                                                   key: Config.Key,
                                                   min: Int = 0, max: Int = 100, step: Int = 1,
                                                   displayParse: (T) -> Int = { it.toInt() },
                                                   valueParse: (Int) -> T = { it as T },
                                                   init: SeekBar.() -> Unit = {}) {
         return seekBarPreference(context.getString(nameRes), if (numFormatRes == 0) null else context.getString(numFormatRes), key, min, max, step, displayParse, valueParse, init)
      }

      fun <T : Number> ViewGroup.seekBarPreference(name: String, numFormat: String? = null,
                                                   key: Config.Key,
                                                   min: Int = 0, max: Int = 100, step: Int = 1,
                                                   displayParse: (T) -> Int = { it.toInt() },
                                                   valueParse: (Int) -> T = { it as T },
                                                   init: SeekBar.() -> Unit = {}) {
         basePreference(name, "") { iconView, nameView, _ ->
            ankoView({ BubbleSeekBar(it) }, 0) {
               setPadding(25, 0, dip(8) + 25, 0)
               minimumHeight = dip(Const.prefItemHeight) / 2
               gravity = Gravity.CENTER
               setMax(max - min)
               incrementProgressBy(step)
               val value = displayParse(Config[key])
               progress = value - min
               progressText = numFormat?.format(valueParse(value)) ?: valueParse(value).toString()
               setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                  override fun onProgressChanged(p0: SeekBar, value: Int, p2: Boolean) {
                     this@ankoView.progressText = numFormat?.format(valueParse(value + min)) ?: valueParse(value + min).toString()
                  }

                  override fun onStartTrackingTouch(p0: SeekBar) {
                  }

                  override fun onStopTrackingTouch(p0: SeekBar) {
                     Config[key] = valueParse(p0.progress + min)
                  }
               })
            }.lparams {
               leftMargin = -25
               rightMargin = -25
               width = matchParent
               height = dip(Const.prefItemHeight) / 2
               below(nameView)
               rightOf(iconView)
               alignParentBottom()
            }
         }
      }

      fun ViewGroup.selectorPreference(nameRes: Int, summaryRes: Int = 0, items: List<Pair<String, Any>>, key: Config.Key, init: () -> Unit = {}) {
         selectorPreference(nameRes, summaryRes, items.map { it.first }, items.map { it.second }, key, init)
      }

      fun ViewGroup.selectorPreference(name: String, summary: String? = null, items: List<Pair<String, Any>>, key: Config.Key, init: () -> Unit = {}) {
         selectorPreference(name, summary, items.map { it.first }, items.map { it.second }, key, init)
      }

      fun ViewGroup.selectorPreference(nameRes: Int, summaryRes: Int = 0, displayName: List<String>, value: List<Any>? = null, key: Config.Key, init: () -> Unit = {}) {
         selectorPreference(context.getString(nameRes), if (summaryRes == 0) null else context.getString(summaryRes), displayName, value, key, init)
      }

      fun ViewGroup.selectorPreference(name: String, summary: String? = null, displayName: List<String>, value: List<Any>? = null, key: Config.Key, init: () -> Unit = {}) {
         relativeLayout {
            backgroundDrawable = getSelectedItemDrawable(context)
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
               context.selector(name, displayName) { dialog, index ->
                  Config[key] = (value ?: displayName)[index]
               }
            }
            lparams {
               width = matchParent
               height = dip(Const.prefItemHeight)
            }
         }
      }

      fun <T> ViewGroup.editTextPreference(nameRes: Int, summaryRes: Int = 0, hintRes: Int = 0, key: Config.Key, displayParser: (T) -> String = { it.toString() }, valueParser: (String) -> T = { it as T }) {
         editTextPreference(context.getString(nameRes),
               if (summaryRes == 0) null else context.getString(summaryRes),
               if (hintRes == 0) null else context.getString(hintRes),
               key,
               displayParser,
               valueParser)
      }

      fun <T> ViewGroup.editTextPreference(name: String, summary: String? = null, hint: String? = null, key: Config.Key, displayParser: (T) -> String = { it.toString() }, valueParser: (String) -> T = { it as T }) {
         basePreference(name, summary) { _, _, _ ->
            setOnClickListener {
               context.alert {
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
         }
      }

      fun ViewGroup.sortingPreference(nameRes: Int, key: Config.Key, displayList: MutableList<String>, valueList: MutableList<String>) {
         sortingPreference(name = context.getString(nameRes), key = key, displayList = displayList, valueList = valueList)
      }

      fun ViewManager.sortingPreference(name: String, key: Config.Key, displayList: MutableList<String>, valueList: MutableList<String>) {
         basePreference(name) { _, _, _ ->
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
         }
      }

      fun ViewManager.preferenceHeader(title: String) {
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

      fun ViewManager.preferenceGroup(init: _LinearLayout.() -> Unit = {}) =
            cardView {
               radius = dip(4).toFloat()
               cardElevation = 0f
               id = View.generateViewId()
               linearLayout {
                  orientation = VERTICAL
                  init()
               }
               lparams {
                  width = matchParent
                  margin = dip(8)
               }
            }

      fun ViewManager.basePreference(name: String, summary: String? = null, icon: Drawable? = null, init: _RelativeLayout.(ImageView, TextView, TextView?) -> Unit = { _, _, _ -> }) =
            relativeLayout {
               id = View.generateViewId()
               backgroundDrawable = getSelectedItemDrawable(context)
               isClickable = true
               setPadding(dip(8), 0, dip(8), 0)
               val iconView = imageView {
                  padding = dip(8)
                  id = View.generateViewId()
                  image = icon
               }.lparams {
                  centerInParent()
                  alignParentStart()
                  width = dip(Const.prefIconWidth)
                  height = dip(Const.prefIconHeight)
               }

               val nameView = textView {
                  id = View.generateViewId()
                  text = name
                  if (summary === null) {
                     gravity = Gravity.CENTER_VERTICAL
                  } else {
                     gravity = Gravity.BOTTOM
                  }
               }.lparams {
                  height = dip(Const.prefItemHeight / 2)
                  rightOf(iconView)
                  if (summary === null) {
                     centerVertically()
                  } else {
                     alignParentTop()
                  }
               }
               var summaryView: TextView? = null
               if (summary !== null) {
                  summaryView = textView {
                     text = summary
                     id = View.generateViewId()
                     gravity = Gravity.TOP
                     textColor = Color.DKGRAY
                  }.lparams {
                     height = dip(Const.prefItemHeight / 2)
                     rightOf(iconView)
                     below(nameView)
                  }
               }
               init(this@relativeLayout, iconView, nameView, summaryView)
               lparams {
                  height = dip(Const.prefItemHeight)
                  width = matchParent
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

