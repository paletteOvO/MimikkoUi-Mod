package me.manhong2112.mimikkouimod.common

import android.content.SharedPreferences
import me.manhong2112.mimikkouimod.common.Utils.log
import org.json.JSONArray



@Suppress("UNCHECKED_CAST")
object Config {
//   
//   enum class Key {
//      DockSwipeToDrawer(false),
//
//      DrawerBlurBackground(false),
//      DrawerDarkBackground(false),
//      DrawerBlurBackgroundBlurRadius(100),
//      DrawerColumnSize(4),
//      DrawerDrawUnderStatusBar(false),
//      DrawerBatSwipeToSearch(false),
//
//      GeneralIconPackFallback(listOf("default")),
//      GeneralIconPackApplyDrawerButton(false),
//      GeneralIconScale(100), // in %
//      GeneralIconScaleApplyDrawerButton(false),
//
//      GeneralTransparentStatusBar(false),
//      GeneralStatusBarColor(0x99000000),
//      GeneralDarkStatusBarIcon(false),
//
//      GeneralShortcutTextSize(10f),
//      GeneralShortcutTextColor(Color.WHITE),
//      GeneralShortcutTextMaxLine(1),
//      GeneralShortcutTextOriginalName(false),
//      GeneralShortcutTextShadowColor(Color.BLACK),
//      GeneralShortcutTextShadowRadius(10f),
//      GeneralShortcutTextShadowDx(0f),
//      GeneralShortcutTextShadowDy(0f);
//
//      private val mDefaultValue: Any
//      val isList: Boolean
//
//      constructor(defaultValue: Any) {
//         this.mDefaultValue = defaultValue
//         isList = false
//      }
//
//      // and we have String, Int, Long, Float ......為甚麼就不能全寫在一起呢...Union Type甚麼的...
//      constructor(defaultValue: List<String>) {
//         this.mDefaultValue = defaultValue
//         isList = true
//      }
//
//
//      fun <T> getDefaultValue(): T {
//         return mDefaultValue as T
//      }
//   }

   private val data = arrayOfNulls<Any>(TypedKey.size)
   private val callbacks = arrayOfNulls<ArrayList<(TypedKey<Any>, Any) -> Unit>>(TypedKey.size) // orz
   private var sharedPreferences: SharedPreferences? = null
   operator fun <T> get(key: TypedKey<T>): T {
      if (data[key.ordinal] === null) {
         data[key.ordinal] = key.defValue
      }
      return data[key.ordinal] as T
   }

   fun bindSharedPref(pref: SharedPreferences) {
      sharedPreferences = pref
   }

   // 這Any搞得我就像在寫動態類型的語言...
   operator fun <T> set(key: TypedKey<T>, value: T) where T : Any {
      data[key.ordinal] = value
      if (sharedPreferences !== null) {
         writeSharedPref(key, sharedPreferences!!)
      }
      callCallback(key, value)
   }

   fun <T : Any> addOnChangeListener(key: TypedKey<T>, callback: (TypedKey<T>, T) -> Unit): Int {
      if (callbacks[key.ordinal] === null) {
         callbacks[key.ordinal] = arrayListOf(callback as (TypedKey<Any>, Any) -> Unit)
      } else {
         callbacks[key.ordinal]!!.add(callback as (TypedKey<Any>, Any) -> Unit)
      }
      return callbacks.size - 1 // Index
   }

   fun <T> removeOnChangeListener(key: TypedKey<T>, callback: (TypedKey<T>, T) -> Unit) {
      callbacks[key.ordinal]?.remove<Any>(callback)
   }

   fun <T> removeOnChangeListener(key: TypedKey<T>, index: Int) {
      callbacks[key.ordinal]?.removeAt(index)
   }

   fun <T> callCallback(key: TypedKey<T>, value: T) where T : Any {
      log("call $key callback")
      callbacks[key.ordinal]?.forEach {
         it(key as TypedKey<Any>, value)
      }
   }

   fun <T> callCallback(key: TypedKey<T>, index: Int, value: T) where T : Any {
      log("call $key callback")
      callbacks[key.ordinal]?.get(index)?.invoke(key as TypedKey<Any>, value)
   }

   fun loadSharedPref(pref: SharedPreferences, callCallback: Boolean = false) {
      TypedKey.values.forEach { key ->
         loadSharedPref(key, pref, callCallback)
      }
   }

   fun <T> loadSharedPref(key: TypedKey<T>, pref: SharedPreferences, callCallback: Boolean = false) {
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
      } ?: key.defValue
      if (callCallback) {
         callCallback(key as TypedKey<Any>, data[key.ordinal]!!)
      }
   }

   fun writeSharedPref(pref: SharedPreferences) {
      val editor = pref.edit()
      TypedKey.values.forEach { key ->
         val value = get(key)
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

   fun <T> writeSharedPref(key: TypedKey<T>, pref: SharedPreferences) {
      val editor = pref.edit()
      val value = get(key)
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