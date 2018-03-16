package me.manhong2112.mimikkouimod

import android.app.Activity
import android.app.Application
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LoadPackage
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.forEachChild


class XposedHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
   fun log(msg: String) {
      println("==MMKUI==Mod==$msg")
   }

   override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {

      if (lpparam.packageName != "com.mimikko.mimikkoui") return
      val cls = XposedHelpers.findClass("com.stub.StubApp", lpparam.classLoader)
      val m = XposedHelpers.findMethodExact(cls, "attachBaseContext", Context::class.java)
      m.isAccessible = true
      val cryAppField = cls.getDeclaredField("יﾞ")
      cryAppField.isAccessible = true
      hookMethod(m, object : XC_MethodHook() {
         @Throws(Throwable::class)
         override fun beforeHookedMethod(param: MethodHookParam) {
            log("beforeHookedMethod attachBaseContext")
         }

         @Throws(Throwable::class)
         override fun afterHookedMethod(param: MethodHookParam) {
            log("afterHookedMethod attachBaseContext")
            val app = cryAppField.get(param.thisObject) as Application

            val cls = XposedHelpers.findClass("com.mimikko.mimikkoui.launcher.activity.Launcher", app.classLoader)


            findAndHookMethod(cls, "onCreate", Bundle::class.java, object : XC_MethodHook() {
               override fun afterHookedMethod(param: MethodHookParam) {
                  val act = param.thisObject as Activity
                  val root = XposedHelpers.getObjectField(act, "root") as RelativeLayout
                  val dock = XposedHelpers.getObjectField(act, "dock") as RelativeLayout
                  dockProcesser(dock)
                  val mAddViewVILp = root::class.java.getMethod("addView", View::class.java, Integer.TYPE, ViewGroup.LayoutParams::class.java)
                  val hook = object : XC_MethodHook() {
                     override fun afterHookedMethod(param: MethodHookParam) {
                        if ((param.thisObject as View).id != root.id) return
                        val inner = param.args[0] as RelativeLayout
                        // drawerLayout : com.mimikko.mimikkoui.launcher.components.drawer.DrawerLayout
                        val drawerLayout = inner.findViewById(MimikkoLayoutID.drawer_layout) as ViewGroup?
                        if (drawerLayout !== null) {
                           val parent = drawerLayout.parent as RelativeLayout
                           val wallpaperManager = WallpaperManager.getInstance(act)
                           if (wallpaperManager.wallpaperInfo === null) {
                              parent.background = ColorDrawable(0)
                              val wallpaper = wallpaperManager.drawable as BitmapDrawable
                              val bitmap = wallpaper.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                              val metrics = DisplayMetrics()
                              act.windowManager.defaultDisplay.getMetrics(metrics)
                              val blurWallpaper = Utils.blur(act, bitmap, 25f)
                              parent.backgroundDrawable = BitmapDrawable(act.resources, blurWallpaper)
//                                 val imageView = ImageView(act)
//                                 imageView.image = BitmapDrawable(act.resources, blurWallpaper)
//                                 imageView.id = 12184
//                                 imageView.layoutParams = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//                                 imageView.scaleType = ImageView.ScaleType.CENTER_CROP
//                                 imageView.fitsSystemWindows = false
//                                 parent.addView(imageView, 0, RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
                           }
                        }
                     }
                  }

                  hookMethod(mAddViewVILp, hook)
               }
            })
         }
      })
      println("==end==")
   }

   fun dockProcesser(dock: RelativeLayout): RelativeLayout {
      return dock
   }

   operator fun String.times(n: Int): String {
      return this.repeat(n)
   }

   fun printView(viewGroup: ViewGroup, intend: Int = 0) {
      log(" " * intend + viewGroup::class.java.canonicalName)
      log(" " * intend + viewGroup.id.toString())
      viewGroup.forEachChild {
         when (it) {
            is ViewGroup ->
               printView(it, intend + 2)
            else -> {
               log(" " * (intend + 2) + it::class.java.canonicalName)
               log(" " * (intend + 2) + it.id.toString())
            }
         }
      }
   }

   fun rootProcesser(root: RelativeLayout): RelativeLayout {
      root.forEachChild {
         log(it.id.toString())
      }
      return root
   }

   @Throws(Throwable::class)
   override fun handleInitPackageResources(resparam: InitPackageResourcesParam) {
      if (resparam.packageName != "com.mimikko.mimikkoui") return

   }
}