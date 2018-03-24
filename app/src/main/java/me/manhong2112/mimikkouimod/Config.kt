package me.manhong2112.mimikkouimod

import android.content.SharedPreferences
import java.io.Serializable

class Config constructor(val type: ConfigType) : Serializable {
   // String|Boolean|Int|Long|Float|StringSet
   enum class ConfigType(val action: String) {
      Drawer(Const.updateDrawerAction),
      Dock(Const.updateDockAction),
   }

   interface ConfigKey {
      fun getDefaultValue(): Any
   }

   enum class Drawer(private val value: Any) : ConfigKey {
      DrawerBlurBackground(false),
      DrawerBlurBackgroundBlurRadius(25f);

      override fun getDefaultValue(): Any {
         return value
      }
   }

   enum class Dock(private val value: Any) : ConfigKey {
      ;

      override fun getDefaultValue(): Any {
         return value
      }
   }

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

   fun <T, T2> get(pref: T2): T where T2 : Enum<*>, T2 : ConfigKey {
      @Suppress("UNCHECKED_CAST")
      when (pref) {
         is Drawer,
         is Dock -> {
            if (data[pref.ordinal] === null) {
               data[pref.ordinal] = pref.getDefaultValue()
            }
            return data[pref.ordinal] as T
         }
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
                  cfg.set(it, pref.all[it.name] ?: it.getDefaultValue())
               }
            ConfigType.Dock ->
               Dock.values().forEach {
                  cfg.set(it, pref.all[it.name] ?: it.getDefaultValue())
               }
         }
         return cfg
      }
   }
}

