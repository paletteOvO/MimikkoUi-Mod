package me.manhong2112.mimikkouimod

import android.app.Activity
import android.app.Application
import android.app.WallpaperManager
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
   lateinit var launcherAct: Activity

   private val updateDrawerReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            drawer ?: return
            val cfg = Config.getDefaultDrawerConfig()
            cfg.fromIntent(intent)

            val value = cfg.get<Boolean>(Config.Drawer.DrawerBlurBackground)
            updateDrawer(launcherAct, drawer!!, arrayOf(value))
         }
      }
   }

   val dock: RelativeLayout by lazy {
      val d = launcherAct.getField<RelativeLayout>("dock")
      updateDock(launcherAct, d)
      d
   }
   val root: RelativeLayout by lazy {
      val r = launcherAct.getField<RelativeLayout>("root")
      updateRoot(launcherAct, r)
      r
   }

   var drawer: ViewGroup? = null
   var workspace: ViewGroup? = null

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
      val launcherClass = findClass("com.mimikko.mimikkoui.launcher.activity.Launcher", app.classLoader)
      IconProvider.ctx = app
      IconProvider.iconPack = IconProvider.IconPack(app, "website.leifs.delta")
      launcherClass.findMethod("onCreate", Bundle::class.java).hook(after = { param ->
         this.app = app
         launcherAct = param.thisObject as Activity
         launcherAct.registerReceiver(updateDrawerReceiver, IntentFilter(Const.updateDrawerAction))
         val mAddViewVILp = root.findMethod("addView", View::class.java, Integer.TYPE, ViewGroup.LayoutParams::class.java)
         mAddViewVILp.hook(after = {
            root
            dock
            rootHook(launcherAct, root, it)
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
            if(drawer === null && innerLayout.findViewById<ViewGroup?>(MimikkoID.drawer_layout) !== null) {
               drawer = innerLayout.findViewById(MimikkoID.drawer_layout) as ViewGroup
               updateDrawer(activity, drawer!!, arrayOf(false))
            }
            if (workspace === null && innerLayout.findViewById<ViewGroup?>(MimikkoID.workspace) !== null) {
               workspace = innerLayout.findViewById(MimikkoID.workspace)
               updateWorkspace(activity, workspace!!)
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


   private var backupBackground: Drawable? = null
   private fun updateDrawer(activity: Activity, drawer: ViewGroup, pref: Array<Any>) {
      val parent = drawer.parent as RelativeLayout
      if(pref[0] as Boolean) {
         val wallpaperManager = WallpaperManager.getInstance(activity)
         if (wallpaperManager.wallpaperInfo === null) {
            backupBackground = backupBackground ?: parent.background
            parent.background = ColorDrawable(Color.TRANSPARENT)
            val wallpaper = wallpaperManager.drawable as BitmapDrawable
            val bitmap = wallpaper.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            val blurWallpaper = Utils.blur(activity, bitmap, 25f)
            parent.backgroundDrawable = BitmapDrawable(activity.resources, blurWallpaper)
         }
      } else {
         parent.background = backupBackground ?: parent.background
      }

   }

   private fun updateWorkspace(activity: Activity, workspace: ViewGroup) {
      return
   }

   private fun updateRoot(activity: Activity, root: RelativeLayout): RelativeLayout {
      return root
   }
}