package me.manhong2112.mimikkouimod.common

import android.content.SharedPreferences

@Suppress("UNCHECKED_CAST")
object Config {
   enum class Key(private val mDefaultValue: Any) {
      DockSwipeToDrawer(false),

      DrawerBlurBackground(false),
      DrawerDarkBackground(false),
      DrawerBlurBackgroundBlurRadius(25),

      GeneralIconPack("website.leifs.delta");

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
         callbacks[key.ordinal]!!(key, value)
      }
   }

   fun loadSharedPref(pref: SharedPreferences, callCallback: Boolean = false) {
      Key.values().forEach { key ->
         loadSharedPref(key, pref, callCallback)
      }
   }

   fun loadSharedPref(key: Key, pref: SharedPreferences, callCallback: Boolean = false) {
      data[key.ordinal] = pref.all[key.name] ?: key.getDefaultValue()
      if (callCallback) {
         callCallback(key, data[key.ordinal]!!)
      }
   }

   fun writeSharedPref(pref: SharedPreferences) {
      val editor = pref.edit()
      Key.values().forEach { key ->
         val value = get(key) as Any
         when (value) {
            is Int -> editor.putInt(key.name, value)
            is Boolean -> editor.putBoolean(key.name, value)
            is Float -> editor.putFloat(key.name, value)
            is Long -> editor.putLong(key.name, value)
            is String -> editor.putString(key.name, value)
            is Set<*> -> editor.putStringSet(key.name, value as Set<String>)
            else -> throw Exception("Type Error: type of $key is not supported")
         }
      }
      editor.apply()
   }

   fun writeSharedPref(key: Key, pref: SharedPreferences) {
      val editor = pref.edit()
      val value = get(key) as Any
      when (value) {
         is Int -> editor.putInt(key.name, value)
         is Boolean -> editor.putBoolean(key.name, value)
         is Float -> editor.putFloat(key.name, value)
         is Long -> editor.putLong(key.name, value)
         is String -> editor.putString(key.name, value)
         is Set<*> -> editor.putStringSet(key.name, value as Set<String>)
         else -> throw Exception("Type Error: type of $key is not supported")
      }
      editor.apply()
   }
}