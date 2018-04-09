package me.manhong2112.mimikkouimod.common

import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class WeakReferenceDelegate<T>(initValue: T? = null) : ReadWriteProperty<Any, T?> {
   private var value: WeakReference<T>? = initValue?.let { WeakReference(it) }

   override fun getValue(thisRef: Any, property: KProperty<*>): T? {
      return value?.get()
   }

   override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
      this.value = value?.let {
         WeakReference(it)
      }
   }

   companion object {
      fun <T> weak(initValue: T? = null): WeakReferenceDelegate<T> {
         return WeakReferenceDelegate(initValue)
      }
   }
}