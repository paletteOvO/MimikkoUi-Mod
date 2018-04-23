@file:Suppress("UNCHECKED_CAST")

package me.manhong2112.mimikkouimod.common

import android.content.Context
import android.content.res.Resources
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
import android.util.ArrayMap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.BuildConfig
import org.jetbrains.anko.forEachChild
import org.jetbrains.anko.opaque
import java.io.File


object Utils {
   val EmptyFunction1: (Any) -> Any = { }
   val EmptyFunction2: (Any, Any) -> Any = { _, _ -> }
   val EmptyFunction3: (Any, Any, Any) -> Any = { _, _, _ -> }

   fun <K, V> arrayMapOf(vararg items: Pair<K, V>): ArrayMap<K, V> {
      val map = ArrayMap<K, V>(items.size)
      items.forEach {
         map[it.first] = it.second
      }
      return map
   }

   fun Drawable.toBitmap(): Bitmap {
      return drawableToBitmap(this)
   }

   fun Bitmap.toDrawable(res: Resources): BitmapDrawable {
      return BitmapDrawable(res, this)
   }

   private fun drawableToBitmap(drawable: Drawable): Bitmap {
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



   fun log(msg: String) {
      if (BuildConfig.DEBUG)
         Log.d(Const.TAG, msg)
   }

   fun log(msg: Any) {
      if (BuildConfig.DEBUG)
         Log.d(Const.TAG, msg.toString())
   }

   operator fun String.times(n: Int): String {
      return this.repeat(n)
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

   fun ViewGroup.findViews(id: Int): List<View> {
      val result = mutableListOf<View>()
      this.forEachChildRecursively {
         if (it.id == id) result.add(it)
      }
      return result
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

   fun getPackageVersion(lpparam: XC_LoadPackage.LoadPackageParam): Pair<String, Int>? {
      return try {
         val parserCls = XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
         val parser = parserCls.newInstance()
         val apkPath = File(lpparam.appInfo.sourceDir)
         val pkg = XposedHelpers.callMethod(parser, "parsePackage", apkPath, 0)
         val versionName = XposedHelpers.getObjectField(pkg, "mVersionName") as String
         val versionCode = XposedHelpers.getIntField(pkg, "mVersionCode")
         Pair(versionName, versionCode)
      } catch (e: Throwable) {
         null
      }
   }


}