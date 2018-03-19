package me.manhong2112.mimikkouimod

import android.content.Intent

class Config private constructor (val type: ConfigType, private val data: Array<java.io.Serializable>) {
   enum class ConfigType {
      Drawer,
      Dock,
   }
   enum class Drawer {
      DrawerBlurBackground,
   }
   enum class Dock {}

   // em...
   fun set(pref: Enum<*>, value: java.io.Serializable) {
      when(pref) {
         is Drawer,
         is Dock ->
            data[pref.ordinal] = value
         else ->
            throw IllegalArgumentException()
      }
   }

   fun <T>get(pref: Enum<*>): T {
      when(pref) {
         is Drawer,
         is Dock ->
            @Suppress("UNCHECKED_CAST")
            return data[pref.ordinal] as T
         else ->
            throw IllegalArgumentException()
      }
   }

   fun toIntent(): Intent {
      when(type) {
         ConfigType.Drawer -> {
            val intent = Intent(Const.updateDrawerAction)
            intent.putExtra("type", ConfigType.Drawer)
            Drawer.values().forEach {
               intent.putExtra(it.name, data[it.ordinal])
            }
            return intent
         }
         ConfigType.Dock -> {
            val intent = Intent(Const.updateDockAction)
            intent.putExtra("type", ConfigType.Dock)
            Dock.values().forEach {
               intent.putExtra(it.name, data[it.ordinal])
            }
            return intent
         }
      }
   }

   fun fromIntent(intent: Intent) {
      when(intent.getSerializableExtra("type")) {
         ConfigType.Drawer -> {
            Drawer.values().forEach {
               data[it.ordinal] = intent.getSerializableExtra(it.name)
            }
         }
         ConfigType.Dock -> {
            Dock.values().forEach {
               data[it.ordinal] = intent.getSerializableExtra(it.name)
            }
         }
      }
   }

   companion object {
      fun getDefaultDrawerConfig(): Config {
         val arr = arrayOf<java.io.Serializable>(
               false
         )
         return Config(ConfigType.Drawer, arr)
      }

      fun getDefaultDockConfig(): Config {
         val arr = arrayOf<java.io.Serializable>(
         )
         return Config(ConfigType.Drawer, arr)
      }
   }

}