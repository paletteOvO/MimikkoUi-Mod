package me.manhong2112.mimikkouimod.xposed


import android.app.Activity
import android.app.Application
import android.content.*
import android.graphics.Canvas
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.IXposedHookInitPackageResources
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.BuildConfig
import me.manhong2112.mimikkouimod.ConfigReceiver
import me.manhong2112.mimikkouimod.SettingsActivity
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.Const.mimikkouiLauncherActName
import me.manhong2112.mimikkouimod.common.Const.mimikkouiPackageName
import me.manhong2112.mimikkouimod.common.Const.supportedVersionCode
import me.manhong2112.mimikkouimod.common.Const.supportedVersionName
import me.manhong2112.mimikkouimod.common.OnSwipeTouchListener
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.findMethod
import me.manhong2112.mimikkouimod.common.Utils.getField
import me.manhong2112.mimikkouimod.common.Utils.hook
import me.manhong2112.mimikkouimod.common.Utils.invokeMethod
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.common.Utils.replace
import me.manhong2112.mimikkouimod.xposed.MimikkoID.drawerSetSpanCountMethodName
import org.jetbrains.anko.contentView
import org.jetbrains.anko.find
import java.io.File
import me.manhong2112.mimikkouimod.common.Config.Key as Cfg


class XposedHook : IXposedHookLoadPackage, IXposedHookInitPackageResources {
   lateinit var app: Application
   lateinit var launcherAct: Activity

   private var drawer: ViewGroup? = null
      set(value) {
         field = value
         initDrawer(launcherAct, value!!)
      }
   private var workspace: ViewGroup? = null
      set(value) {
         field = value
         initWorkspace(launcherAct, workspace!!)
      }

   private val configUpdateReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            val key = intent.getStringExtra("Key")
            val value = intent.getSerializableExtra("Value")
            log("receive config $key -> $value")
            Config[Config.Key.valueOf(key)] = value
         }
      }
   }
   private val wallpaperUpdateReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            updateDrawerBackground(null, null)
         }
      }
   }
   private val dock: RelativeLayout by lazy {
      val d = launcherAct.getField<RelativeLayout>("dock")
      initDock(launcherAct, d)
      d
   }

   private val root: RelativeLayout by lazy {
      val r = launcherAct.getField<RelativeLayout>("root")
      initRoot(launcherAct, r)
      r
   }

   override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
      if (lpparam.packageName != mimikkouiPackageName) return
      getPackageVersion(lpparam)?.run {
         val versionName = first
         val versionCode = second
         if (versionCode != supportedVersionCode ||
               versionName != supportedVersionName) {
            return
         }
      } ?: run {
         log("null version info")
      }
//      val stubClass = XposedHelpers.findClass("com.stub.StubApp", lpparam.classLoader)
//
//      val m = stubClass.findMethod("attachBaseContext", Context::class.java)
//      m.hook(after = { param ->
//         val app = param.thisObject.getField(appVariableName) as Application
//         realAppHook(app)
//      })

      // 既然殼沒了, 那...
      // 設計時想了不少, 偏偏沒想到會把殼去掉..
      val launcherClass = findClass(mimikkouiLauncherActName, lpparam.classLoader)
      launcherClass.findMethod("onCreate", Bundle::class.java).hook(after = { param ->
         val app = (param.thisObject as Activity).application
         onCreateHook(param)
         realAppHook(app)
      })
   }

   private fun getPackageVersion(lpparam: XC_LoadPackage.LoadPackageParam): Pair<String, Int>? {
      return try {
         val parserCls = XposedHelpers.findClass("android.content.pm.PackageParser", lpparam.classLoader)
         val parser = parserCls.newInstance()
         val apkPath = File(lpparam.appInfo.sourceDir)
         val pkg = XposedHelpers.callMethod(parser, "parsePackage", apkPath, 0)
         val versionName = XposedHelpers.getObjectField(pkg, "mVersionName") as String
         val versionCode = XposedHelpers.getIntField(pkg, "mVersionCode")
         Pair(versionName, versionCode)
      } catch (e: Throwable) {
         null
      }
   }

   @Throws(Throwable::class)
   override fun handleInitPackageResources(resparam: InitPackageResourcesParam) {
      if (resparam.packageName != mimikkouiPackageName) return
   }

   private fun onCreateHook(param: XC_MethodHook.MethodHookParam) {
      launcherAct = param.thisObject as Activity
      app = launcherAct.application
      app.registerReceiver(configUpdateReceiver, IntentFilter(Const.configUpdateAction))
      app.registerReceiver(wallpaperUpdateReceiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)) // well at least it still exist

      bindConfigUpdateListener()
      loadConfig(app)

      IconProvider.update(app)
      DrawerBackground.update(app)

      val mAddViewVILp = root.findMethod("addView", View::class.java, Integer.TYPE, ViewGroup.LayoutParams::class.java)
      mAddViewVILp.hook(after = {
         if ((it.thisObject as View).id != root.id) return@hook
         rootHook(launcherAct, root, it)
      })
   }
   private fun realAppHook(app: Application) {
      // com.mimikko.mimikkoui.launcher.components.cell.CellView
      val launcherClass = findClass(mimikkouiLauncherActName, app.classLoader)
//      launcherClass.findMethod("onCreate", Bundle::class.java).hook(after = ::onCreateHook)

      launcherClass.findMethod("load", Integer.TYPE).hook {
         // com.mimikko.mimikkoui.launcher.activity.Launcher -> load
         p ->
         // 鬼知道這數字甚麼意思, 我討厭殼 (好吧我承認是我渣
         if (p.args[0] as Int == 10) {
            dock
         }
      }

      val appItemEntityClass = findClass("com.mimikko.common.beans.models.AppItemEntity", app.classLoader)
      appItemEntityClass.findMethod("getIcon").replace(::iconHook)

      injectSetting(app)

      findClass("com.mimikko.mimikkoui.launcher.components.shortcut.Shortcut", app.classLoader)
            .findMethod("onDraw", Canvas::class.java)
            .hook { p ->
               // log("Shortcut Constructor")
               val shortcut = p.thisObject as TextView
               shortcut.setShadowLayer(
                     Config[Config.Key.GeneralShortcutTextShadowRadius],
                     Config[Config.Key.GeneralShortcutTextShadowDx],
                     Config[Config.Key.GeneralShortcutTextShadowDy],
                     Config[Config.Key.GeneralShortcutTextShadowColor])
               shortcut.maxLines = Config[Config.Key.GeneralShortcutTextMaxLine]
               shortcut.setTextColor(Config.get<Int>(Config.Key.GeneralShortcutTextColor))
               shortcut.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config.get(Config.Key.GeneralShortcutTextSize))
//               val bubbleItem = shortcut.invokeMethod("getBubbleItem") as Any
//               val icon = bubbleItem.invokeMethod("getIcon") as Bitmap
//
//               val scale = Config.get<Int>(Config.Key.GeneralIconScale) / 100f
//               val scaledWidth = (icon.width * scale / 2).roundToInt()
//               val scaledHeight = (icon.height * scale / 2).roundToInt()
               // shortcut.invokeMethod("setIconRect", Rect(-scaledWidth, -scaledHeight, scaledWidth, scaledHeight)) as Any
            }
   }

   private fun updateDrawerBackground(k: Config.Key?, v: Any?) {
      DrawerBackground.update(app, drawer)
   }

   private fun bindConfigUpdateListener() {
      Config.setOnChangeListener(Config.Key.DrawerBlurBackground, ::updateDrawerBackground)
      Config.setOnChangeListener(Config.Key.DrawerBlurBackgroundBlurRadius, ::updateDrawerBackground)
      Config.setOnChangeListener(Config.Key.DrawerDarkBackground, ::updateDrawerBackground)
      Config.setOnChangeListener(Config.Key.DrawerColumnSize, { _, _: Int ->
         drawer?.let {
            setDrawerColumnSize(it)
         }
      })

      Config.setOnChangeListener(Config.Key.GeneralDarkStatusBarIcon, { k, v: Boolean ->
         if (v) {
            launcherAct.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
         } else {
            launcherAct.window.decorView.systemUiVisibility = 0
         }
      })

      Config.setOnChangeListener(Config.Key.GeneralTransparentStatusBar, { _, v: Boolean ->
         if (v) {
            with(launcherAct.window) {
               setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            }
         } else {
            launcherAct.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
         }
      })

      Config.setOnChangeListener(Config.Key.GeneralIconPack, { _, _: String ->
         log("setOnChangeListener GeneralIconPack")
         IconProvider.update(app)
      })

   }

   private fun loadConfig(ctx: Context) {
      log("send loadConfig")
      val intent = Intent(Const.loadConfigAction)
      intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
      intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
      intent.setClassName(BuildConfig.APPLICATION_ID, ConfigReceiver::class.java.name)
      ctx.sendBroadcast(intent)
   }

   private fun iconHook(param: XC_MethodHook.MethodHookParam): Any {
      val name = param.thisObject.invokeMethod<ComponentName>("getId")
      return IconProvider.getIcon(name) ?: throw Utils.CallOriginalMethod()
   }

   private fun rootHook(activity: Activity, root: RelativeLayout, param: XC_MethodHook.MethodHookParam) {
      val innerLayout = param.args[0] as ViewGroup
      when (innerLayout) {
         is RelativeLayout -> {
            // it can be drawerLayout or Workspace
            // drawerLayout : com.mimikko.mimikkoui.launcher.components.drawer.DrawerLayout
            if (workspace === null && innerLayout.findViewById<ViewGroup?>(MimikkoID.workspace) !== null) {
               workspace = innerLayout.findViewById(MimikkoID.workspace) as ViewGroup
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
         val setting = contentView.find<View>(MimikkoID.app_settings).parent!! as LinearLayout

         val modSettingView =
               findClass("com.mimikko.common.ui.settinglist.ListItem", app.classLoader)
                     .getDeclaredConstructor(Context::class.java)
                     .newInstance(launcherAct) as RelativeLayout
         modSettingView.invokeMethod<Unit>("setClickable", true)
         modSettingView.invokeMethod<Unit>("setLabel", "MimikkoUI-Mod")
         modSettingView.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
         modSettingView.setOnClickListener {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName(BuildConfig.APPLICATION_ID, SettingsActivity::class.java.name)
            launcherSettingAct.startActivity(intent)
         }
         setting.addView(modSettingView)
      }
   }

   private fun initDock(act: Activity, dock: RelativeLayout) {
      val drawerBtn = dock.findViewById(MimikkoID.drawerButton) as View?
      drawerBtn?.run {
         setOnTouchListener(
               object : OnSwipeTouchListener(act) {
                  override fun onSwipeTop() {
                     if (Config[Config.Key.DockSwipeToDrawer]) {
                        performClick()
                        drawer ?: run {
                           drawer = launcherAct.findViewById(MimikkoID.drawer_layout) as ViewGroup
                        }
                     }
                  }

                  override fun onClick() {
                     performClick()
                     drawer ?: run {
                        drawer = launcherAct.findViewById(MimikkoID.drawer_layout) as ViewGroup
                     }
                  }
               }
         )
      }
   }

   private fun initWorkspace(activity: Activity, workspace: ViewGroup) {
      return
   }

   private fun initRoot(activity: Activity, root: RelativeLayout): RelativeLayout {
      return root
   }

   private fun initDrawer(activity: Activity, drawer: ViewGroup) {
      DrawerBackground.setDrawerBackground(drawer)
      setDrawerColumnSize(drawer)
   }

   private fun setDrawerColumnSize(drawer: ViewGroup) {
      drawer.invokeMethod<Any>("getLayoutManager").invokeMethod<Unit>(drawerSetSpanCountMethodName, Config[Config.Key.DrawerColumnSize])
   }
}