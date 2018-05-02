package me.manhong2112.mimikkouimod.xposed

import android.app.Activity
import android.app.WallpaperManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
   private var future: Future<Drawable?>? = null
   private var imageView: ImageView? by weak<ImageView?>()

   fun enable(drawer: ViewGroup) {
      val parent = drawer.parent as RelativeLayout
      (drawer.layoutParams as RelativeLayout.LayoutParams).setMargins(0, parent.topPadding, 0, 0)
      parent.padding = 0

      parent.findViewById<View?>(drawerBackgroundId) ?: run {
         imageView = ImageView(drawer.context)
         imageView?.let {
            it.scaleType = ImageView.ScaleType.CENTER_CROP
            it.id = drawerBackgroundId
            it.image = future?.get()
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
      val v = Config[K.DrawerBlurBackgroundBlurRadius]
      // load and blur background
      future?.cancel(true)
      future = doAsyncResult {
         val img = if (Config[K.DrawerBlurBackground] && v != 0) {
            log("future br1")
            val wallpaperManager = WallpaperManager.getInstance(act)

            val scaleFactor = 1f - 9.5f * v / 10000f
            val wallpaperBitmap = (wallpaperManager.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val bitmap = Utils.downscale(wallpaperBitmap, scaleFactor)
            wallpaperBitmap.recycle()

            val wallpaper =
                  if (Config[K.DrawerDarkBackground])
                     Utils.darken(Utils.blur(act, bitmap, v.toFloat() / 50f + 5))
                  else
                     Utils.blur(act, bitmap, v.toFloat() / 50f + 5)
            BitmapDrawable(act.resources, wallpaper)
         } else {
            log("future br2")
            null
         }
         act.runOnUiThread {
            imageView?.image = img
         }
         img
      }
      log("update end")
   }
}