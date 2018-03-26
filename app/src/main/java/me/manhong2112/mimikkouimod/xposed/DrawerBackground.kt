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

object DrawerBackground {
   private var drawerBackground: Drawable? = null
   private var drawerBGBackup: Drawable? = null
   private var counter = 0

   fun setDrawerBackground(drawer: View) {
      drawerBackground ?: return
      val parent = drawer.parent as RelativeLayout
      drawerBGBackup = drawerBGBackup ?: parent.background
      parent.background = drawerBackground
      log("drawerBackground end")
   }

   fun update(ctx: Context) {
      if (Config[Config.Key.DrawerBlurBackground]) {
         val wallpaperManager = WallpaperManager.getInstance(ctx)
         val bitmap = (wallpaperManager.drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)
         val wallpaper =
               if (Config[Config.Key.DrawerDarkBackground])
                  Utils.darken(Utils.blur(ctx, bitmap, Config[Config.Key.DrawerBlurBackgroundBlurRadius]))
               else
                  Utils.blur(ctx, bitmap, Config[Config.Key.DrawerBlurBackgroundBlurRadius])
         log("set drawerBackground br1")
         drawerBackground = BitmapDrawable(ctx.resources, wallpaper)
      } else {
         log("set drawerBackground br2")
         drawerBackground = drawerBGBackup
      }
      log("update end")
   }
}