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

object DrawerBackground {
   private var drawerBackground: Drawable? = null
   private var imageView: ImageView? by weak()
   fun enable(drawer: ViewGroup) {
      drawerBackground ?: return
      val parent = drawer.parent as RelativeLayout
      // drawerBGBackup = drawerBGBackup ?: parent.background
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
   }

   fun disable(drawer: ViewGroup) {
      val parent = drawer.parent as ViewGroup
      parent.findViewById<View?>(drawerBackgroundId)?.let {
         parent.removeView(it)
      }
   }

   private val lock: Any = Any()
   fun update(act: Activity, drawer: View? = null) {
      log("update start")
      val x = Config.get<Int>(Config.Key.DrawerBlurBackgroundBlurRadius)
      doAsync {
         synchronized(lock) {
            if (Config[Config.Key.DrawerBlurBackground] && x != 0) {
               val wallpaperManager = WallpaperManager.getInstance(act)

               val scaleFactor = 1 - x / 1000f
               val wallpaperBitmap = (wallpaperManager.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
               val bitmap = Utils.downscale(wallpaperBitmap, scaleFactor)
               wallpaperBitmap.recycle()

               val wallpaper =
                     if (Config[Config.Key.DrawerDarkBackground])
                        Utils.darken(Utils.blur(act, bitmap, Config.get<Int>(Config.Key.DrawerBlurBackgroundBlurRadius).toFloat() / 40f))
                     else
                        Utils.blur(act, bitmap, Config.get<Int>(Config.Key.DrawerBlurBackgroundBlurRadius).toFloat() / 40f)
               log("set drawerBackground br1")
               drawerBackground = BitmapDrawable(act.resources, wallpaper)
               act.runOnUiThread {
                  imageView?.let {
                     it.image = drawerBackground
                  }
               }
            }
         }
      }

      log("update end")
   }
}