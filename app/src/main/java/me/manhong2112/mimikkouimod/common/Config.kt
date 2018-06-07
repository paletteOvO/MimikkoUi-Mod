@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package me.manhong2112.mimikkouimod.common

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import me.manhong2112.mimikkouimod.BuildConfig
import me.manhong2112.mimikkouimod.ConfigReceiver
import me.manhong2112.mimikkouimod.common.Const.keyExtraName
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.xposed.MimikkoUI
import org.json.JSONArray
import java.io.Serializable
import me.manhong2112.mimikkouimod.common.TypedKey as K


object Config {
   private val data = arrayOfNulls<Any>(K.size)
   private val callbacks = arrayOfNulls<ArrayList<(K<Any>, Any) -> Unit>>(K.size) // orz
   private var sharedPreferences: SharedPreferences? = null
   operator fun <T> get(key: K<T>): T where T : Any {
      data[key.ordinal] = data[key.ordinal] ?: sharedPreferences?.let {
         it.all[key.name]?.let { value ->
            when {
               key.defValue is List<*> -> JSONArray(value as String).toList<String>()
               else -> value
            }
         }
      } ?: key.defValue
      return data[key.ordinal] as T
   }

   operator fun <T> set(key: K<T>, value: T) where T : Any {
      data[key.ordinal] = value
      if (sharedPreferences !== null) {
         writeSharedPref(key, sharedPreferences!!)
      }
      callCallback(key, value)
   }

   fun <T> addOnChangeListener(key: K<T>, callback: (K<T>, T) -> Unit): Int where T : Any {
      if (callbacks[key.ordinal] === null) {
         callbacks[key.ordinal] = arrayListOf(callback as (K<Any>, Any) -> Unit)
      } else {
         callbacks[key.ordinal]!!.add(callback as (K<Any>, Any) -> Unit)
      }
      return callbacks.size - 1 // Index
   }

   fun <T> removeOnChangeListener(key: K<T>, callback: (K<T>, T) -> Unit) where T : Any {
      callbacks[key.ordinal]?.remove<Any>(callback)
   }

   fun <T> removeOnChangeListener(key: K<T>, index: Int) where T : Any {
      callbacks[key.ordinal]?.removeAt(index)
   }

   fun <T> callCallback(key: K<T>, value: T) where T : Any {
      log("call $key callback")
      callbacks[key.ordinal]?.forEach {
         it(key as K<Any>, value)
      }
   }

   fun <T> callCallback(key: K<T>, index: Int, value: T) where T : Any {
      log("call $key callback")
      callbacks[key.ordinal]?.get(index)?.invoke(key as K<Any>, value)
   }

   fun <T> writeSharedPref(key: K<T>, pref: SharedPreferences) where T : Any {
      val editor = pref.edit()
      val value = get(key)
      when (value) {
         is Int -> editor.put(key.name, value)
         is Boolean -> editor.put(key.name, value)
         is Float -> editor.put(key.name, value)
         is Long -> editor.put(key.name, value)
         is String -> editor.put(key.name, value)
         is Set<*> -> editor.put(key.name, value as Set<String>)
         is List<*> -> {
            editor.put(key.name, value.toJSONArray().toString())
         }
         else -> {
            // if(value::class.java.getAnnotation())
            throw Exception("Type Error: type of $key is not supported")
         }
      }
      editor.apply()
   }

   fun bindSharedPref(pref: SharedPreferences) {
      sharedPreferences = pref
   }

   fun writeSharedPref(pref: SharedPreferences) {
      val editor = pref.edit()
      K.values.forEach { key ->
         val value = get(key)
         when (value) {
            is Int -> editor.put(key.name, value)
            is Boolean -> editor.put(key.name, value)
            is Float -> editor.put(key.name, value)
            is Long -> editor.put(key.name, value)
            is String -> editor.put(key.name, value)
            is Set<*> -> editor.put(key.name, value as Set<String>)
            is List<*> -> {
               editor.put(key.name, value.toJSONArray().toString())
            }
            else -> throw Exception("Type Error: type of $key is not supported")
         }
      }
      editor.apply()
   }


   fun updateConfig(ctx: Context, key: K<*>) {
      log("updateConfig $key")
      ctx.sendBroadcast(
            Intent(Const.updateConfigAction)
                  .putExtra(Const.keyExtraName, key.name)
                  .putExtra(Const.valueExtraName, Config[key] as Serializable)
                  .setPackage(MimikkoUI.packageName))
   }

   fun loadConfig(ctx: Context, key: K<*>) {
      log("loadConfig $key")
      ctx.sendBroadcast(
            Intent(Const.loadConfigAction)
                  .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                  .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                  .putExtra(keyExtraName, key.name)
                  .setPackage(MimikkoUI.packageName))
   }

   fun loadConfig(ctx: Context) {
      Utils.log("loadConfig")
      ctx.sendBroadcast(
            Intent(Const.loadConfigAction)
                  .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                  .addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                  .setClassName(BuildConfig.APPLICATION_ID, ConfigReceiver::class.java.name))
   }
}


private inline fun SharedPreferences.Editor.put(key: String, value: Int) = putInt(key, value)

private inline fun SharedPreferences.Editor.put(key: String, value: Boolean) = putBoolean(key, value)

private inline fun SharedPreferences.Editor.put(key: String, value: Float) = putFloat(key, value)

private inline fun SharedPreferences.Editor.put(key: String, value: Long) = putLong(key, value)

private inline fun SharedPreferences.Editor.put(key: String, value: String) = putString(key, value)

private inline fun SharedPreferences.Editor.put(key: String, value: Set<String>) = putStringSet(key, value)