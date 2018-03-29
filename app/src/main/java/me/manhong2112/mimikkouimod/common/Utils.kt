@file:Suppress("UNCHECKED_CAST")

package me.manhong2112.mimikkouimod.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LightingColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import me.manhong2112.mimikkouimod.BuildConfig
import org.jetbrains.anko.forEachChild
import org.jetbrains.anko.opaque
import java.lang.reflect.Constructor
import java.lang.reflect.Member
import java.lang.reflect.Method


object Utils {
   fun drawableToBitmap(drawable: Drawable): Bitmap {
      if (drawable is BitmapDrawable) {
         if (drawable.bitmap != null) {
            return drawable.bitmap
         }
      }
      val bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
         Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
      } else {
         Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
      }

      val canvas = Canvas(bitmap)
      drawable.setBounds(0, 0, canvas.width, canvas.height)
      drawable.draw(canvas)
      return bitmap
   }

   fun upscale(height: Int, width: Int, bitmap: Bitmap): Bitmap {
      var widthFactor = 0f
      var heightFactor = 0f
      if (width > bitmap.width) {
         widthFactor = width.toFloat() / bitmap.width
      }
      if (height > bitmap.height) {
         heightFactor = height.toFloat() / bitmap.height
      }

      val upscaleFactor = Math.max(widthFactor, heightFactor)
      if (upscaleFactor <= 0) {
         return bitmap
      }

      val scaledWidth = (bitmap.width * upscaleFactor).toInt()
      val scaledHeight = (bitmap.height * upscaleFactor).toInt()
      val scaled = Bitmap.createScaledBitmap(
            bitmap,
            scaledWidth,
            scaledHeight, false)

      val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas()
      canvas.setBitmap(result)

      val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
      if (widthFactor > heightFactor) {
         canvas.drawBitmap(scaled, 0f, ((height - scaledHeight) / 2).toFloat(), paint)
      } else {
         canvas.drawBitmap(scaled, ((width - scaledWidth) / 2).toFloat(), 0f, paint)
      }

      return result
   }

   fun downscale(bitmap: Bitmap, scaleFactor: Float): Bitmap {
      return Bitmap.createScaledBitmap(bitmap, (bitmap.width * scaleFactor).toInt(), (bitmap.height * scaleFactor).toInt(), true)
   }

   fun blur(ctx: Context, image: Bitmap, blurRadius: Float): Bitmap {
      log("blur")
      val outputBitmap = Bitmap.createBitmap(image)
      val rs = RenderScript.create(ctx)
      val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

      val input = Allocation.createFromBitmap(rs, outputBitmap, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SHARED)
      val output = Allocation.createTyped(rs, input.type)

      script.setRadius(blurRadius)
      script.setInput(input)
      script.forEach(output)
      output.copyTo(outputBitmap)

      rs.destroy()
      log("blur-ed")
      return outputBitmap
   }

   fun darken(bitmap: Bitmap): Bitmap {

      val paint = Paint()
      val filter = LightingColorFilter(0x888888.opaque, 0)
      paint.colorFilter = filter

      val canvas = Canvas(bitmap)
      canvas.drawBitmap(bitmap, 0f, 0f, paint)
      return bitmap
   }

   fun hookMethod(method: Member, before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      return XposedBridge.hookMethod(method, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            before(param)
         }

         override fun afterHookedMethod(param: MethodHookParam) {
            after(param)
         }
      })
   }

   fun replaceMethod(method: Method, replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      return XposedBridge.hookMethod(method, object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            try {
               val result = replacement(param)
               param.result = result
            } catch (_: CallOriginalMethod) {
            }
         }
      })
   }

   class CallOriginalMethod : Throwable()

   fun log(msg: String) {
      if (BuildConfig.DEBUG)
         Log.d(Const.TAG, msg)
   }

   operator fun String.times(n: Int): String {
      return this.repeat(n)
   }

   fun <T> Any.getField(name: String): T {
      return XposedHelpers.getObjectField(this, name) as T
   }

   fun <T> Any.invokeMethod(name: String, vararg args: Any): T {
      return this::class.java.findMethod(name, *args.map {
         when (it) {
            is Byte -> java.lang.Byte.TYPE
            is Short -> java.lang.Short.TYPE
            is Int -> java.lang.Integer.TYPE
            is Long -> java.lang.Long.TYPE
            is Float -> java.lang.Float.TYPE
            is Double -> java.lang.Double.TYPE
            is Boolean -> java.lang.Boolean.TYPE
            is Char -> java.lang.Character.TYPE
            else -> it::class.java
         }
      }.toTypedArray()).invoke(this, *args) as T
   }

   fun Class<*>.findMethod(methodName: String, vararg typeList: Class<*>): Method {
      val cls = this
      return try {
         cls.getDeclaredMethod(methodName, *typeList).also {
            it.isAccessible = true
         }
      } catch (e: NoSuchMethodException) {
         this.superclass?.findMethod(methodName, *typeList) ?: throw e
      }
   }

   fun Any.findMethod(methodName: String, vararg typeList: Class<*>): Method {
      return this.javaClass.findMethod(methodName, *typeList)
   }

   fun Method.hook(before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return hookMethod(this, before = before, after = after)
   }

   fun <T> Constructor<T>.hook(before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}) {
      hookMethod(this, before, after)
   }

   fun Class<*>.hookAllMethod(name: String, before: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }, after: (Method, XC_MethodHook.MethodHookParam) -> Unit = { _, _ -> }) {
      this.declaredMethods.filter { it.name == name }.map { it.hook(before = { p -> before(it, p) }, after = { p -> after(it, p) }) }
   }

   fun Method.replace(replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return replaceMethod(this, replacement = replacement)
   }

   fun printCurrentStackTrace(deep: Int = -1) {
      log("!StackTrace")
      val stackTrace = (Exception()).stackTrace
      val i = if (deep < 0) stackTrace.size else deep
      var n = 0
      for (ele in stackTrace) {
         if (n >= i) break
         n += 1
         log("!  ${ele.className} -> ${ele.methodName}")
      }
      log("!====")
   }

   fun ViewGroup.printView(intend: Int = 0) {
      val viewGroup = this
      log("${" " * intend} ${viewGroup::class.java.canonicalName} : ${viewGroup::class.java.superclass.canonicalName} # ${viewGroup.id}")
      viewGroup.forEachChild {
         when (it) {
            is ViewGroup ->
               it.printView(intend + 2)
            else -> {
               log("${" " * (intend + 2)} ${it::class.java.canonicalName} : ${it::class.java.superclass.canonicalName} # ${it.id}")
            }
         }
      }
   }

   fun ViewGroup.forEachChildRecursively(func: (View) -> Unit) {
      val viewGroup = this
      func(viewGroup)
      viewGroup.forEachChild {
         when (it) {
            is ViewGroup ->
               it.forEachChildRecursively(func)
            else -> {
               func(it)
            }
         }
      }
   }

   fun Any.printAllField() {
      val obj = this
      log(obj::class.java.canonicalName)
      obj::class.java.declaredFields.map {
         it.isAccessible = true
         log("  ${it.name} :: ${it.type.canonicalName} : ${it.type.superclass?.canonicalName} = ${it.get(obj)}")
      }
   }


}