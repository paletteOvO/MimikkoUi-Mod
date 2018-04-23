package me.manhong2112.mimikkouimod.common

import java.lang.ref.WeakReference
import kotlin.reflect.KProperty

class WeakReferenceDelegate<T>(initValue: T? = null) {
   private var value: WeakReference<T>? = initValue?.let { WeakReference(it) }

   operator fun getValue(thisRef: Nothing?, property: KProperty<*>): T? {
      return value?.get()
   }

   operator fun setValue(thisRef: Nothing?, property: KProperty<*>, value: T?) {
      this.value = value?.let {
         WeakReference(it)
      }
   }

   operator fun getValue(thisRef: Any?, property: KProperty<*>): T? {
      return value?.get()
   }

   operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
      this.value = value?.let {
         WeakReference(it)
      }
   }
   companion object {
      fun <T> weak(initValue: T? = null): WeakReferenceDelegate<T> {
         return WeakReferenceDelegate<T>(initValue)
      }
   }
}