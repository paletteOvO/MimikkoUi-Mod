package me.manhong2112.mimikkouimod

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LightingColorFilter
import android.graphics.Paint
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import org.jetbrains.anko.opaque
import java.lang.reflect.Method


class Utils {
   companion object {
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

      fun blur(ctx: Context, image: Bitmap, blurRadius: Float): Bitmap {
         val width = image.width
         val height = image.height

         val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
         val outputBitmap = Bitmap.createBitmap(inputBitmap)

         val rs = RenderScript.create(ctx)
         val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
         val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
         val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
         theIntrinsic.setRadius(blurRadius)
         theIntrinsic.setInput(tmpIn)
         theIntrinsic.forEach(tmpOut)
         val paint = Paint()
         val filter = LightingColorFilter(0x888888.opaque, 0)
         paint.colorFilter = filter
         tmpOut.copyTo(outputBitmap)

         val canvas = Canvas(outputBitmap)
         canvas.drawBitmap(outputBitmap, 0f, 0f, paint)


         return outputBitmap
      }

      fun hookMethod(method: Method, before: (XC_MethodHook.MethodHookParam) -> Unit, after: (XC_MethodHook.MethodHookParam) -> Unit): XC_MethodHook.Unhook {
         return XposedBridge.hookMethod(method, object: XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
               before(param)
            }

            override fun afterHookedMethod(param: MethodHookParam) {
               after(param)
            }
         })
      }
   }

}