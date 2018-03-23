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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.Const.mimikkouiPackageName
import me.manhong2112.mimikkouimod.MimikkoID.appVariableName
import me.manhong2112.mimikkouimod.Utils.findMethod
import me.manhong2112.mimikkouimod.Utils.getField
import me.manhong2112.mimikkouimod.Utils.hook
import me.manhong2112.mimikkouimod.Utils.invokeMethod
import me.manhong2112.mimikkouimod.Utils.log
import me.manhong2112.mimikkouimod.Utils.replace
import org.jetbrains.anko.backgroundDrawable
import org.jetbrains.anko.contentView
import org.jetbrains.anko.find
import org.jetbrains.anko.onClick


class XposedHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
   lateinit var app: Application
   lateinit var launcherAct: Activity

   private var drawer: ViewGroup? = null
   private var drawerConfig: Config? = null
      set(value) {
         field = value
         log("set drawerConfig")
         drawer ?: return
         updateDrawer(launcherAct, drawer!!, value!!)
      }


   private var dockConfig: Config? = null
      set(value) {
         field = value
         updateDock(launcherAct, dock)
      }

   private val updateDrawerReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            log("received updateDrawerAction")
            log(intent.action)
            drawerConfig = intent.getSerializableExtra("Config") as Config
         }
      }
   }
   private val updateDockReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            dockConfig = intent.getSerializableExtra("Config") as Config
         }
      }
   }

   private val dock: RelativeLayout by lazy {
      val d = launcherAct.getField<RelativeLayout>("dock")
      updateDock(launcherAct, d)
      d
   }
   private val root: RelativeLayout by lazy {
      val r = launcherAct.getField<RelativeLayout>("root")
      updateRoot(launcherAct, r)
      r
   }

   var workspace: ViewGroup? = null

   override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
      if (lpparam.packageName != mimikkouiPackageName) return
      val stubClass = XposedHelpers.findClass("com.stub.StubApp", lpparam.classLoader)
      val m = stubClass.findMethod("attachBaseContext", Context::class.java)
      m.hook(after = { param ->
         val app = param.thisObject.getField(appVariableName) as Application
         realAppHook(app)
      })

   }

   @Throws(Throwable::class)
   override fun handleInitPackageResources(resparam: InitPackageResourcesParam) {
      if (resparam.packageName != mimikkouiPackageName) return
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
         loadConfig(app)
         val mAddViewVILp = root.findMethod("addView", View::class.java, Integer.TYPE, ViewGroup.LayoutParams::class.java)
         mAddViewVILp.hook(after = {
            root
            dock
            rootHook(launcherAct, root, it)
         })
      })

//      launcherClass.findMethod("setContentView", View::class.java).hook {
//          param ->
//          log("Call setContentView(${param.args.joinToString(", ") {it.toString()}})")
//      }

      val appItemEntityClass = findClass("com.mimikko.common.beans.models.AppItemEntity", app.classLoader)
      appItemEntityClass.findMethod("getIcon").replace(::iconHook)

      injectSetting(app)
   }

   private fun loadConfig(ctx: Context) {
      log("loadConfig")
      val intent = Intent(Const.loadConfigAction)
      intent.setClassName(BuildConfig.APPLICATION_ID, SettingsActivity::class.java.name)
      ctx.startActivity(intent)
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
            if (drawer === null && innerLayout.findViewById<ViewGroup?>(MimikkoID.drawer_layout) !== null) {
               drawer = innerLayout.findViewById(MimikkoID.drawer_layout) as ViewGroup
               drawerConfig ?: return
               updateDrawer(activity, drawer!!, drawerConfig!!)
            }
            if (workspace === null && innerLayout.findViewById<ViewGroup?>(MimikkoID.workspace) !== null) {
               workspace = innerLayout.findViewById(MimikkoID.workspace)
               updateWorkspace(activity, workspace!!)
            }
         }
         else -> {
            log("rootAddView ${innerLayout::class.java.canonicalName}")
         }
      }
   }

   private fun injectSetting(app: Application) {
      val launcherSettingClass = findClass("com.mimikko.mimikkoui.launcher.activity.LauncherSettingActivity", app.classLoader)
      lateinit var launcherSettingAct: Activity
      launcherSettingClass.findMethod("onCreate", Bundle::class.java).hook { param ->
         launcherSettingAct = param.thisObject as Activity
         val contentView = launcherSettingAct.contentView!! as ViewGroup
         val app_setting = contentView.find<View>(MimikkoID.app_settings)
         val setting = app_setting.parent!! as LinearLayout

         val modSettingView =
               findClass("com.mimikko.common.ui.settinglist.ListItem", app.classLoader)
                     .getDeclaredConstructor(Context::class.java)
                     .newInstance(launcherAct) as RelativeLayout
         modSettingView.invokeMethod<Unit>("setClickable", true)
         modSettingView.invokeMethod<Unit>("setLabel", "MimikkoUI-Mod")
         modSettingView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
         modSettingView.onClick {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(BuildConfig.APPLICATION_ID, SettingsActivity::class.java.name)
            launcherSettingAct.startActivity(intent)
         }
         setting.addView(modSettingView)
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
   private fun updateDrawer(activity: Activity, drawer: ViewGroup, cfg: Config) {
      log("update drawer")
      val parent = drawer.parent as RelativeLayout
      if (cfg.get(Config.Drawer.DrawerBlurBackground)!!) {
         val wallpaperManager = WallpaperManager.getInstance(activity)
         if (wallpaperManager.wallpaperInfo === null) {
            backupBackground = backupBackground ?: parent.background
            parent.background = ColorDrawable(Color.TRANSPARENT)
            val wallpaper = wallpaperManager.drawable as BitmapDrawable
            val bitmap = wallpaper.bitmap.copy(Bitmap.Config.ARGB_8888, true)
            val metrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(metrics)
            val blurWallpaper = Utils.blur(activity, bitmap, cfg.get(Config.Drawer.DrawerBlurBackgroundBlurRadius))
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