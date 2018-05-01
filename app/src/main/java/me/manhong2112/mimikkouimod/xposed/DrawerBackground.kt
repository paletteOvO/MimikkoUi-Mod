package me.manhong2112.mimikkouimod.xposed

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const.drawerBackgroundId
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.common.WeakReferenceDelegate.Companion.weak
import org.jetbrains.anko.*
import java.util.concurrent.Future
import me.manhong2112.mimikkouimod.common.TypedKey as K

object DrawerBackground {
   private val drawerBackground: BitmapDrawable?
      get() {
         return drawerBackgroundFuture?.get()
      }
   private var drawerBackgroundFuture: Future<BitmapDrawable?>? = null
   private var imageView: ImageView? by weak<ImageView?>()

   fun enable(drawer: ViewGroup) {
      drawerBackground ?: return
      val parent = drawer.parent as RelativeLayout
      (drawer.layoutParams as RelativeLayout.LayoutParams).setMargins(0, parent.topPadding, 0, 0)
      parent.padding = 0

      parent.findViewById<View?>(drawerBackgroundId) ?: run {
         imageView = ImageView(drawer.context)
         imageView?.let {
            it.scaleType = ImageView.ScaleType.CENTER_CROP
            it.id = drawerBackgroundId
            it.image = drawerBackground
            parent.addView(it, 0, RelativeLayout.LayoutParams(matchParent, matchParent))
         }
      }
      imageView?.visibility = View.VISIBLE
   }

   fun disable(drawer: ViewGroup) {
      imageView?.visibility = View.GONE
   }

   fun update(act: Activity, drawer: View? = null) {
      log("update start")
      val v = Config.get<Int>(K.DrawerBlurBackgroundBlurRadius)
      drawerBackground?.bitmap?.recycle()
      drawerBackgroundFuture?.cancel(true)
      drawerBackgroundFuture = if (Config[K.DrawerBlurBackground] && v != 0) {
         log("set drawerBackground br1")
         doAsyncResult {
            val wallpaperManager = WallpaperManager.getInstance(act)

            val scaleFactor = 1 - v / 1000f
            val wallpaperBitmap = (wallpaperManager.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val bitmap = Utils.downscale(wallpaperBitmap, scaleFactor)
            wallpaperBitmap.recycle()

            val wallpaper =
                  if (Config[K.DrawerDarkBackground])
                     Utils.darken(Utils.blur(act, bitmap, v.toFloat() / 40f))
                  else
                     Utils.blur(act, bitmap, v.toFloat() / 40f)
            val result = BitmapDrawable(act.resources, wallpaper)

            act.runOnUiThread {
               imageView?.image = result
            }
            return@doAsyncResult result
         }
      } else {
         log("set drawerBackground br2")
         act.runOnUiThread {
            imageView?.image = null
         }
         null
      }
      log("update end")
   }
}