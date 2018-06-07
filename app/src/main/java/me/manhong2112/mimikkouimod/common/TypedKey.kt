package me.manhong2112.mimikkouimod.common

import android.graphics.Color
import android.support.annotation.ColorInt
import me.manhong2112.mimikkouimod.setting.Neko
import me.manhong2112.mimikkouimod.setting.NoAction
import org.jetbrains.anko.collections.forEachWithIndex
import org.jetbrains.anko.withAlpha

open class TypedKey<out T>(val defValue: T) where T : Any {
   object DrawerColumnSize : TypedKey<Int>(4)

   object GeneralIconPackFallback : TypedKey<List<String>>(listOf("default"))
   object GeneralIconPackApplyDrawerButton : TypedKey<Boolean>(false)
   object GeneralIconScale : TypedKey<Int>(100) // in %
   object GeneralIconScaleApplyDrawerButton : TypedKey<Boolean>(false)

   object GeneralStatusBarColor : TypedKey<@ColorInt Int>(Color.BLACK.withAlpha(0x99))
   object GeneralDarkStatusBarIcon : TypedKey<Boolean>(false)

   object GeneralInjectSetting : TypedKey<Boolean>(false)

   object GeneralShortcutTextSize : TypedKey<Float>(10f)
   object GeneralShortcutTextColor : TypedKey<@ColorInt Int>(Color.WHITE)
   object GeneralShortcutTextMaxLine : TypedKey<Int>(1)
   object GeneralShortcutTextShadowColor : TypedKey<@ColorInt Int>(Color.BLACK)
   object GeneralShortcutTextShadowRadius : TypedKey<Float>(10f)
   object GeneralShortcutTextShadowDx : TypedKey<Float>(0f)
   object GeneralShortcutTextShadowDy : TypedKey<Float>(0f)

   object NeKoMiMiSwipeUpGesture : TypedKey<String>(NoAction.toString())
   object NeKoMiMiSwipeDownGesture : TypedKey<String>(NoAction.toString())
   object NeKoMiMiSwipeLeftGesture : TypedKey<String>(NoAction.toString())
   object NeKoMiMiSwipeRightGesture : TypedKey<String>(NoAction.toString())
   object NeKoMiMiClickGesture : TypedKey<String>(Neko("Open Drawer").toString())
   object NeKoMiMiDoubleClickGesture : TypedKey<String>(NoAction.toString())
   object NeKoMiMiLongClickGesture : TypedKey<String>(NoAction.toString())

   @Suppress("LeakingThis")
   val name: String = this::class.java.simpleName

   val ordinal: Int by lazy {
      values.forEachWithIndex { index, value ->
         if (value::class.java === this::class.java) {
            return@lazy index
         }
      }
      throw IllegalStateException()
   }

   override fun equals(other: Any?): Boolean {
      return other === this
   }

   override fun hashCode(): Int {
      return ordinal
   }

   override fun toString(): String {
      return "TypedKey(${this.name}<${this.defValue::class.java.simpleName}>)"
   }

   companion object {
      fun valueOf(name: String): TypedKey<Any> {
         values.forEach {
            if (it.name == name) {
               return it
            }
         }
         throw IllegalStateException()
      }

      val values: Array<TypedKey<Any>> = TypedKey::class.java.declaredClasses.mapNotNull {
         if (it.superclass === TypedKey::class.java) {
            it.getField("INSTANCE").get(it) as? TypedKey<Any>
         } else null
      }.toTypedArray()

      val size: Int = values.size
   }
}
