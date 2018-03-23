package me.manhong2112.mimikkouimod

import android.content.SharedPreferences
import java.io.Serializable

class Config constructor(val type: ConfigType) : Serializable {
   // String|Boolean|Int|Long|Float|StringSet

   enum class ConfigType {
      Drawer,
      Dock,
   }

   enum class Drawer {
      DrawerBlurBackground,
      DrawerBlurBackgroundBlurRadius,
   }

   enum class Dock

   private val data: Array<Any?> by lazy {
      when (type) {
         ConfigType.Drawer -> arrayOfNulls<Any>(Drawer.values().size)
         ConfigType.Dock -> arrayOfNulls<Any>(Dock.values().size)
      }
   }

   // em...
   fun set(pref: Enum<*>, value: Any) {
      when (value) {
         is String, is Boolean, is Int, is Long, is Float, is Set<*> -> {
         }
         else ->
            throw IllegalArgumentException()
      }
      when (pref) {
         is Drawer,
         is Dock ->
            data[pref.ordinal] = value
         else ->
            throw IllegalArgumentException()
      }
   }

   fun <T> get(pref: Enum<*>): T {
      @Suppress("UNCHECKED_CAST")
      when (pref) {
         is Drawer,
         is Dock ->
            return data[pref.ordinal] as T
         else ->
            throw IllegalArgumentException()
      }
   }

   companion object {
      fun fromSharedPref(type: ConfigType, pref: SharedPreferences): Config {
         val cfg = Config(type)
         when (type) {
            ConfigType.Drawer ->
               Drawer.values().forEach {
                  if (pref.all[it.name] !== null) cfg.set(it, pref.all[it.name]!!)
               }
            ConfigType.Dock ->
               Dock.values().forEach {
                  if (pref.all[it.name] !== null) cfg.set(it, pref.all[it.name]!!)
               }
         }
         return cfg
      }
   }
}

