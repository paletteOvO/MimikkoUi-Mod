package me.manhong2112.mimikkouimod.common

import android.content.SharedPreferences
import android.graphics.Color
import me.manhong2112.mimikkouimod.common.Utils.log
import org.json.JSONArray



@Suppress("UNCHECKED_CAST")
object Config {
   enum class Key {
      DockSwipeToDrawer(false),

      DrawerBlurBackground(false),
      DrawerDarkBackground(false),
      DrawerBlurBackgroundBlurRadius(100),
      DrawerColumnSize(4),
      DrawerDrawUnderStatusBar(false),

      GeneralIconPackFallback(listOf("default")),
      GeneralIconScale(100), // in %

      GeneralTransparentStatusBar(false),
      GeneralDarkStatusBarIcon(false),

      GeneralShortcutTextSize(10f),
      GeneralShortcutTextColor(Color.WHITE),
      GeneralShortcutTextMaxLine(1),
      GeneralShortcutTextOriginalName(false),
      GeneralShortcutTextShadowColor(Color.BLACK),
      GeneralShortcutTextShadowRadius(10f),
      GeneralShortcutTextShadowDx(0f),
      GeneralShortcutTextShadowDy(0f);


      private val mDefaultValue: Any
      val isList: Boolean

      constructor(defaultValue: Any) {
         this.mDefaultValue = defaultValue
         isList = false
      }

      // and we have String, Int, Long, Float ......為甚麼就不能全寫在一起呢...Union Type甚麼的...
      constructor(defaultValue: List<String>) {
         this.mDefaultValue = defaultValue
         isList = true
      }


      fun <T> getDefaultValue(): T {
         return mDefaultValue as T
      }
   }

   private val data = arrayOfNulls<Any>(Key.values().size)
   private val callbacks = arrayOfNulls<kotlin.Function2<Key, *, Unit>>(Key.values().size)
   private var sharedPreferences: SharedPreferences? = null
   operator fun <R> get(key: Key): R {
      if (data[key.ordinal] === null) {
         data[key.ordinal] = key.getDefaultValue()
      }
      return data[key.ordinal] as R
   }

   fun bindSharedPref(pref: SharedPreferences) {
      sharedPreferences = pref
   }

   // 這Any搞得我就像在寫動態類型的語言...
   operator fun set(key: Key, value: Any) {
      data[key.ordinal] = value
      if (sharedPreferences !== null) {
         writeSharedPref(key, sharedPreferences!!)
      }
      callCallback(key, value)
   }

   fun <T> setOnChangeListener(key: Key, callback: (Key, T) -> Unit) {
      callbacks[key.ordinal] = callback
   }

   fun callCallback(key: Key, value: Any) {
      if (callbacks[key.ordinal] !== null) {
         log("call $key callback")
         callbacks[key.ordinal]!!(key, value)
      }
   }

   fun loadSharedPref(pref: SharedPreferences, callCallback: Boolean = false) {
      Key.values().forEach { key ->
         loadSharedPref(key, pref, callCallback)
      }
   }

   fun loadSharedPref(key: Key, pref: SharedPreferences, callCallback: Boolean = false) {
      log("loading $key")
      data[key.ordinal] = when {
         key.isList -> {
            pref.all[key.name]?.let {
               val arr = JSONArray(it as String)
               val result = arrayOfNulls<Any>(arr.length())
               for (i in 0 until arr.length()) {
                  result[i] = arr.get(i)
               }
               (result as Array<Any>).asList()
            }
         }
         else -> {
            pref.all[key.name]
         }
      } ?: key.getDefaultValue()
      if (callCallback) {
         callCallback(key, data[key.ordinal]!!)
      }
   }

   fun writeSharedPref(pref: SharedPreferences) {
      val editor = pref.edit()
      Key.values().forEach { key ->
         val value = get(key) as Any
         when {
            value is Int -> editor.put(key.name, value)
            value is Boolean -> editor.put(key.name, value)
            value is Float -> editor.put(key.name, value)
            value is Long -> editor.put(key.name, value)
            value is String -> editor.put(key.name, value)
            value is Set<*> -> editor.put(key.name, value as Set<String>)
            value is List<*> && key.isList -> {
               val jsonArr = JSONArray()
               value.forEach {
                  jsonArr.put(it)
               }
               editor.put(key.name, jsonArr.toString())
            }
            else -> throw Exception("Type Error: type of $key is not supported")
         }
      }
      editor.apply()
   }

   fun writeSharedPref(key: Key, pref: SharedPreferences) {
      val editor = pref.edit()
      val value = get(key) as Any
      when {
         value is Int -> editor.put(key.name, value)
         value is Boolean -> editor.put(key.name, value)
         value is Float -> editor.put(key.name, value)
         value is Long -> editor.put(key.name, value)
         value is String -> editor.put(key.name, value)
         value is Set<*> -> editor.put(key.name, value as Set<String>)
         value is List<*> && key.isList -> {
            val jsonArr = JSONArray()
            value.forEach {
               jsonArr.put(it)
            }
            editor.put(key.name, jsonArr.toString())
         }
         else -> throw Exception("Type Error: type of $key is not supported")
      }
      editor.apply()
   }

   inline fun SharedPreferences.Editor.put(key: String, value: Int) {
      this.putInt(key, value)
   }

   inline fun SharedPreferences.Editor.put(key: String, value: Boolean) {
      this.putBoolean(key, value)
   }

   inline fun SharedPreferences.Editor.put(key: String, value: Float) {
      this.putFloat(key, value)
   }

   inline fun SharedPreferences.Editor.put(key: String, value: Long) {
      this.putLong(key, value)
   }

   inline fun SharedPreferences.Editor.put(key: String, value: String) {
      this.putString(key, value)
   }

   inline fun SharedPreferences.Editor.put(key: String, value: Set<String>) {
      this.putStringSet(key, value)
   }
}