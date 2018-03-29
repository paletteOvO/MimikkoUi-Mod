package me.manhong2112.mimikkouimod.xposed

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.RelativeLayout
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.log
import org.jetbrains.anko.doAsync

object DrawerBackground {
   private var drawerBackground: Drawable? = null
   private var drawerBGBackup: Drawable? = null

   fun setDrawerBackground(drawer: View) {
      drawerBackground ?: return
      val parent = drawer.parent as RelativeLayout
      drawerBGBackup = drawerBGBackup ?: parent.background
      parent.background = drawerBackground
      log("drawerBackground end")
   }

   val lock: Any = Any()
   fun update(ctx: Context, drawer: View? = null) {
      log("update start")
      val x = Config.get<Int>(Config.Key.DrawerBlurBackgroundBlurRadius)
      doAsync {
         synchronized(lock) {
            if (Config[Config.Key.DrawerBlurBackground] && x != 0) {
               val wallpaperManager = WallpaperManager.getInstance(ctx)

               val scaleFactor = 1 - x / 1998f
               val wallpaperBitmap = (wallpaperManager.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
               val bitmap = Utils.downscale(wallpaperBitmap, scaleFactor)
               wallpaperBitmap.recycle()

               val wallpaper =
                     if (Config[Config.Key.DrawerDarkBackground])
                        Utils.darken(Utils.blur(ctx, bitmap, Config.get<Int>(Config.Key.DrawerBlurBackgroundBlurRadius).toFloat() / 40f))
                     else
                        Utils.blur(ctx, bitmap, Config.get<Int>(Config.Key.DrawerBlurBackgroundBlurRadius).toFloat() / 40f)
               log("set drawerBackground br1")
               drawerBackground = BitmapDrawable(ctx.resources, wallpaper)
            } else {
               log("set drawerBackground br2")
               drawerBackground = drawerBGBackup
            }
            drawer?.let {
               setDrawerBackground(it)
            }
         }
      }

      log("update end")
   }
}