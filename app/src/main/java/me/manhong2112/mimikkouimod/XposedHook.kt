package me.manhong2112.mimikkouimod

import android.app.Activity
import android.app.Application
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
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
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.Utils.log
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.forEachChild
import java.lang.reflect.Method


class XposedHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {


   fun <T> Any.getField(name: String): T {
      return XposedHelpers.getObjectField(this, name) as T
   }

   fun Method.hook(before: (XC_MethodHook.MethodHookParam) -> Unit = {}, after: (XC_MethodHook.MethodHookParam) -> Unit = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return Utils.hookMethod(this, before = before, after = after)
   }

   fun Method.replace(replacement: (XC_MethodHook.MethodHookParam) -> Any = {}): XC_MethodHook.Unhook {
      this.isAccessible = true
      return Utils.replaceMethod(this, replacement = replacement)
   }

   fun <T> Any.invokeMethod(name: String, vararg args: Any): T {
      return XposedHelpers.findMethodExact(this::class.java, name, *args.map { it::class.java }.toTypedArray()).invoke(this, *args) as T
   }

   override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
      if (lpparam.packageName != "com.mimikko.mimikkoui") return
      val stubClass = XposedHelpers.findClass("com.stub.StubApp", lpparam.classLoader)
      val m = Utils.findMethod(stubClass, "attachBaseContext", Context::class.java)
      Utils.hookMethod(m, after = { param ->
         val app = getObjectField(param.thisObject, "יﾞ") as Application
         realAppHook(app)
      })
   }

   private fun realAppHook(app: Application) {
      // com.mimikko.mimikkoui.launcher.components.cell.CellView
      val launcherClass = findClass("com.mimikko.mimikkoui.launcher.activity.Launcher", app.classLoader)
      IconProvider.ctx = app
      IconProvider.iconPack = IconProvider.IconPack(app, "website.leifs.delta")
      Utils.findMethod(launcherClass, "onCreate", Bundle::class.java).hook(after = { param ->
         val act = param.thisObject as Activity
         val root = act.getField("root") as RelativeLayout
         val dock = act.getField("dock") as RelativeLayout
         val mAddViewVILp = Utils.findMethod(root::class.java, "addView", View::class.java, Integer.TYPE, ViewGroup.LayoutParams::class.java)
         Utils.hookMethod(mAddViewVILp, after = {
            rootLayoutHook(root)
            dockHook(dock)
            rootAddView(act, root, it)
         })
      })

      val appItemEntityClass = findClass("com.mimikko.common.beans.models.AppItemEntity", app.classLoader)
      Utils.findMethod(appItemEntityClass, "getIcon").replace { param ->
         val name = param.thisObject.invokeMethod<ComponentName>("getId")
         return@replace IconProvider.getIcon(name.toString())
               ?: throw Utils.CallOriginalMethod()
      }
   }

   private fun drawerButton(drawerButton: View) {
      val btn = drawerButton as ImageView
      // btn.image = drawerButton.context.resources.getDrawable(android.R.drawable.btn_star)
   }

   private fun dockHook(dock: RelativeLayout): RelativeLayout {
      val drawerBtn = dock.findViewById(0x7f0900a3) as Any?
      if (dock.findViewById(0x7f0900a3) as Any? !== null) {
         drawerButton(drawerBtn as View)
      }
      return dock
   }

   private fun iconHook() {

   }

   private fun drawerHook(activity: Activity, drawer: ViewGroup) {
      val parent = drawer.parent as RelativeLayout
      val wallpaperManager = WallpaperManager.getInstance(activity)
      if (wallpaperManager.wallpaperInfo === null) {
         parent.background = ColorDrawable(0)
         val wallpaper = wallpaperManager.drawable as BitmapDrawable
         val bitmap = wallpaper.bitmap.copy(Bitmap.Config.ARGB_8888, true)
         val metrics = DisplayMetrics()
         activity.windowManager.defaultDisplay.getMetrics(metrics)
         val blurWallpaper = Utils.blur(activity, bitmap, 25f)
         parent.backgroundDrawable = BitmapDrawable(activity.resources, blurWallpaper)
      }
   }

   private fun rootAddView(activity: Activity, root: RelativeLayout, param: XC_MethodHook.MethodHookParam) {
      if ((param.thisObject as View).id != root.id) return
      val innerLayout = param.args[0] as ViewGroup
      when (innerLayout) {
         is RelativeLayout -> {
            // it can be drawerLayout or Workspace
            // drawerLayout : com.mimikko.mimikkoui.launcher.components.drawer.DrawerLayout
            val drawerLayout = innerLayout.findViewById(MimikkoLayoutID.drawer_layout) as ViewGroup?
            if (drawerLayout !== null) {
               drawerHook(activity, drawerLayout)
               return
            }
            val workSpace = innerLayout.findViewById(MimikkoLayoutID.workspace) as ViewGroup?
         }
         else -> {
            log("rootAddView ${innerLayout::class.java.canonicalName}")
         }
      }
   }

   operator fun String.times(n: Int): String {
      return this.repeat(n)
   }

   fun printView(viewGroup: ViewGroup, intend: Int = 0) {
      log("${" " * intend} ${viewGroup::class.java.canonicalName} : ${viewGroup::class.java.superclass.canonicalName} # ${viewGroup.id}")
      viewGroup.forEachChild {
         when (it) {
            is ViewGroup ->
               printView(it, intend + 2)
            else -> {
               log("${" " * (intend + 2)} ${it::class.java.canonicalName} : ${it::class.java.superclass.canonicalName} # ${it.id}")
            }
         }
      }
   }

   fun printAllField(obj: Any) {
      log(obj::class.java.canonicalName)
      obj::class.java.declaredFields.map {
         it.isAccessible = true
         log("  ${it.name} :: ${it.type.canonicalName} : ${it.type.superclass?.canonicalName} = ${it.get(obj)}")
      }
   }

   fun rootLayoutHook(root: RelativeLayout): RelativeLayout {
      return root
   }

   inline fun printCurrentStackTrace(deep: Int = -1) {
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

   @Throws(Throwable::class)
   override fun handleInitPackageResources(resparam: InitPackageResourcesParam) {
      if (resparam.packageName != "com.mimikko.mimikkoui") return

   }
}