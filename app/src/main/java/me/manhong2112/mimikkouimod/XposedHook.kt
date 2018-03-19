package me.manhong2112.mimikkouimod

import android.app.Activity
import android.app.Application
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.MimikkoID.appVariableName
import me.manhong2112.mimikkouimod.Utils.findMethod
import me.manhong2112.mimikkouimod.Utils.getField
import me.manhong2112.mimikkouimod.Utils.hook
import me.manhong2112.mimikkouimod.Utils.invokeMethod
import me.manhong2112.mimikkouimod.Utils.log
import me.manhong2112.mimikkouimod.Utils.replace
import org.jetbrains.anko.backgroundDrawable


class XposedHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
   lateinit var app: Application
   override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
      if (lpparam.packageName != "com.mimikko.mimikkoui") return
      val stubClass = XposedHelpers.findClass("com.stub.StubApp", lpparam.classLoader)
      val m = stubClass.findMethod("attachBaseContext", Context::class.java)
      m.hook(after = { param ->
         val app = param.thisObject.getField(appVariableName) as Application
         realAppHook(app)
      })
   }

   @Throws(Throwable::class)
   override fun handleInitPackageResources(resparam: InitPackageResourcesParam) {
      if (resparam.packageName != "com.mimikko.mimikkoui") return
   }

   private fun realAppHook(app: Application) {
      // com.mimikko.mimikkoui.launcher.components.cell.CellView
      this.app = app
      val launcherClass = findClass("com.mimikko.mimikkoui.launcher.activity.Launcher", app.classLoader)
      IconProvider.ctx = app
      IconProvider.iconPack = IconProvider.IconPack(app, "website.leifs.delta")
      launcherClass.findMethod("onCreate", Bundle::class.java).hook(after = { param ->
         val act = param.thisObject as Activity
         val root = act.getField("root") as RelativeLayout
         val dock = act.getField("dock") as RelativeLayout
         val mAddViewVILp = root.findMethod("addView", View::class.java, Integer.TYPE, ViewGroup.LayoutParams::class.java)
         mAddViewVILp.hook(after = {
            updateRoot(act, root)
            updateDock(act, dock)
            rootHook(act, root, it)
         })
      })

      val appItemEntityClass = findClass("com.mimikko.common.beans.models.AppItemEntity", app.classLoader)
      appItemEntityClass.findMethod("getIcon").replace(::iconHook)
   }

   private fun iconHook(param: XC_MethodHook.MethodHookParam): Any {
      val name = param.thisObject.invokeMethod<ComponentName>("getId")
      return IconProvider.getIcon(name.toString())
            ?: throw Utils.CallOriginalMethod()
   }

   private fun rootHook(activity: Activity, root: RelativeLayout, param: XC_MethodHook.MethodHookParam) {
      if ((param.thisObject as View).id != root.id) return
      val innerLayout = param.args[0] as ViewGroup
      when (innerLayout) {
         is RelativeLayout -> {
            // it can be drawerLayout or Workspace
            // drawerLayout : com.mimikko.mimikkoui.launcher.components.drawer.DrawerLayout
            val drawerLayout = innerLayout.findViewById(MimikkoID.drawer_layout) as ViewGroup?
            if (drawerLayout !== null) {
               updateDrawer(activity, drawerLayout)
               return
            }
            val workSpace = innerLayout.findViewById(MimikkoID.workspace) as ViewGroup?
            if (workSpace !== null) {
               updateWorkspace(activity, workSpace)
               return
            }
         }
         else -> {
            log("rootAddView ${innerLayout::class.java.canonicalName}")
         }
      }
   }

   private fun updateDrawerButton(activity: Activity, drawerButton: View) {
      val btn = drawerButton as ImageView
      // btn.image = drawerButton.context.resources.getDrawable(android.R.drawable.btn_star)
   }

   private fun updateDock(activity: Activity, dock: RelativeLayout): RelativeLayout {
      val drawerBtn = dock.findViewById(MimikkoID.drawerButton) as Any?
      if (dock.findViewById(MimikkoID.drawerButton) as Any? !== null) {
         updateDrawerButton(activity, drawerBtn as View)
      }
      return dock
   }

   private fun updateDrawer(activity: Activity, drawer: ViewGroup) {
      val parent = drawer.parent as RelativeLayout
      val wallpaperManager = WallpaperManager.getInstance(activity)
      if (wallpaperManager.wallpaperInfo === null) {
         parent.background = ColorDrawable(Color.TRANSPARENT)
         val wallpaper = wallpaperManager.drawable as BitmapDrawable
         val bitmap = wallpaper.bitmap.copy(Bitmap.Config.ARGB_8888, true)
         val metrics = DisplayMetrics()
         activity.windowManager.defaultDisplay.getMetrics(metrics)
         val blurWallpaper = Utils.blur(activity, bitmap, 25f)
         parent.backgroundDrawable = BitmapDrawable(activity.resources, blurWallpaper)
      }
   }

   private fun updateWorkspace(activity: Activity, workspace: ViewGroup) {
      return
   }

   private fun updateRoot(activity: Activity, root: RelativeLayout): RelativeLayout {
      return root
   }
}