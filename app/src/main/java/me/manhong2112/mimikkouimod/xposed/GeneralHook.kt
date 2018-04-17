package me.manhong2112.mimikkouimod.xposed


import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.BuildConfig
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.OnSwipeTouchListener
import me.manhong2112.mimikkouimod.common.ReflectionUtils
import me.manhong2112.mimikkouimod.common.ReflectionUtils.findMethod
import me.manhong2112.mimikkouimod.common.ReflectionUtils.getField
import me.manhong2112.mimikkouimod.common.ReflectionUtils.hook
import me.manhong2112.mimikkouimod.common.ReflectionUtils.hookAsync
import me.manhong2112.mimikkouimod.common.ReflectionUtils.invokeMethod
import me.manhong2112.mimikkouimod.common.ReflectionUtils.replace
import me.manhong2112.mimikkouimod.common.Utils.findViews
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.setting.SettingsActivity
import org.jetbrains.anko.contentView
import org.jetbrains.anko.find
import org.jetbrains.anko.forEachChild
import org.jetbrains.anko.image
import kotlin.math.roundToInt
import me.manhong2112.mimikkouimod.common.Config.Key as K

open class GeneralHook {
   private lateinit var classLoader: ClassLoader
   private lateinit var app: Application
   private lateinit var launcherAct: Activity
   private val launcherActCls by lazy {
      XposedHelpers.findClass(MimikkoUI.launcherClsName, classLoader)
   }

   private var workspace: ViewGroup? = null
      set(value) {
         field = value
         value?.let {
            initWorkspace(launcherAct, it)
         }
      }
   private val dock: RelativeLayout
      get() {
         return launcherAct.getField<RelativeLayout>("dock").also {
            initDock(launcherAct, it)
         }
      }
   private var dockLayout: ViewGroup? = null
   private var drawerBtn: ImageView? = null
   private val root: RelativeLayout
      get() {
         return launcherAct.getField<RelativeLayout>("root").also {
            initRoot(launcherAct, it)
         }
      }

   fun onLoad(clsLoader: ClassLoader, lpparam: XC_LoadPackage.LoadPackageParam) {
      classLoader = clsLoader
      launcherActCls.findMethod("onCreate", Bundle::class.java).hookAsync(after = { param ->
         log("onCreate ${param.args.joinToString(", ")}")
         launcherAct = param.thisObject as Activity
         app = launcherAct.application

         bindConfigUpdateListener()

         IconProvider.update(app)

         if (param.args[0] === null) {
            injectSetting(app)
         }

         realAppHook(app)
      })

   }

   private fun realAppHook(app: Application) {
      // com.mimikko.mimikkoui.launcher.components.cell.CellView
      val mAddViewVILp = root.findMethod("addView", View::class.java, Integer.TYPE, ViewGroup.LayoutParams::class.java)
      mAddViewVILp.hookAsync(after = {
         if ((it.thisObject as View).id == root.id) {
            rootHook(launcherAct, root, it)
         }
      })

      launcherActCls.findMethod("load", Integer.TYPE).hook {
         // com.mimikko.mimikkoui.launcher.activity.Launcher -> load
         p ->
         // 鬼知道這數字甚麼意思, 我討厭殼 (好吧我承認是我渣
         log("launcherClass load")
         if (p.args[0] as Int == 10) {
            log("load 10")
            dockLayout = dock.find(MimikkoUI.id.dock_layout)
         }
      }

      val appItemEntityClass = findClass("com.mimikko.common.beans.models.AppItemEntity", app.classLoader)

      appItemEntityClass.findMethod("getIcon").replace { param ->
         val name = param.thisObject.invokeMethod<ComponentName>("getId")
         return@replace IconProvider.getIcon(name.toString()) ?: throw ReflectionUtils.CallOriginalMethod
      }

      appItemEntityClass.findMethod("getLabel").replace { param ->
         if (Config[Config.Key.GeneralShortcutTextOriginalName]) {
            val name = param.thisObject.invokeMethod<ComponentName>("getId")
            return@replace LabelProvider.getLabel(app, name)
         } else {
            throw ReflectionUtils.CallOriginalMethod
         }
      }

      findClass("com.mimikko.mimikkoui.launcher.components.shortcut.Shortcut", app.classLoader)
            .declaredConstructors
            .forEach {
               it.hook { p ->
                  val shortcut = p.thisObject as TextView
                  shortcut.setShadowLayer(
                        Config[Config.Key.GeneralShortcutTextShadowRadius],
                        Config[Config.Key.GeneralShortcutTextShadowDx],
                        Config[Config.Key.GeneralShortcutTextShadowDy],
                        Config[Config.Key.GeneralShortcutTextShadowColor])
                  shortcut.maxLines = Config[Config.Key.GeneralShortcutTextMaxLine]
                  shortcut.setTextColor(Config.get<Int>(Config.Key.GeneralShortcutTextColor))
                  shortcut.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config[Config.Key.GeneralShortcutTextSize])

                  val s = (launcherAct.resources.getDimension(MimikkoUI.dimen.app_icon_size) / 2 * Config.get<Int>(Config.Key.GeneralIconScale) / 100f).roundToInt()

                  val rect = p.thisObject.invokeMethod("getIconRect") as Rect
                  rect.set(-s, -s, s, s)
               }
            }
   }


   private fun bindConfigUpdateListener() {
      Config.addOnChangeListener(Config.Key.GeneralDarkStatusBarIcon, { k, v: Boolean ->
         if (v) {
            launcherAct.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
         } else {
            launcherAct.window.decorView.systemUiVisibility = 0
         }
      })
      Config.addOnChangeListener(Config.Key.GeneralTransparentStatusBar, { _, v: Boolean ->
         if (v) {
            with(launcherAct.window) {
               setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            }
         } else {
            launcherAct.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
         }
      })
      Config.addOnChangeListener(Config.Key.GeneralIconPackFallback, { _, _: Any ->
         log("setOnChangeListener GeneralIconPackFallback")
         IconProvider.update(app)
         if (Config[K.GeneralIconPackApplyDrawerButton]) {
            drawerBtn?.image = BitmapDrawable(launcherAct.resources, IconProvider.getIcon(Const.drawerBtnDrawableComponentName))
         }
      })
      Config.addOnChangeListener(Config.Key.GeneralIconPackApplyDrawerButton, { _, v: Boolean ->
         log("setOnChangeListener GeneralIconPackApplyDrawerButton")
         if (v) {
            drawerBtn?.image = BitmapDrawable(launcherAct.resources, IconProvider.getIcon(Const.drawerBtnDrawableComponentName))
         } else {
            drawerBtn?.image = BitmapDrawable(launcherAct.resources, IconProvider.DefaultIconPack.getIcon(Const.drawerBtnDrawableComponentName))
         }
      })
      Config.addOnChangeListener(Config.Key.GeneralIconScaleApplyDrawerButton, { _, v: Boolean ->
         if (v) {
            drawerBtn?.scaleX = Config.get<Int>(Config.Key.GeneralIconScale) / 100f
            drawerBtn?.scaleY = Config.get<Int>(Config.Key.GeneralIconScale) / 100f
         } else {
            drawerBtn?.scaleX = 1f
            drawerBtn?.scaleY = 1f
         }
      })
      Config.addOnChangeListener(Config.Key.GeneralIconScale, { _, scale: Int ->
         log("setOnChangeListener GeneralIconScale")
         val s = (launcherAct.resources.getDimension(MimikkoUI.dimen.app_icon_size) / 2 * scale / 100f).roundToInt()
         dockLayout?.forEachChild {
            (it as ViewGroup).getChildAt(1).let {
               it.invokeMethod<Any>("setIconRect", Rect(-s, -s, s, s))
               it.invalidate()
            }
         }
         workspace?.findViews(MimikkoUI.id.bubble)?.forEach {
            it.invokeMethod<Any>("setIconRect", Rect(-s, -s, s, s))
            it.invalidate()
         }

         if (Config[K.GeneralIconScaleApplyDrawerButton]) {
            drawerBtn?.scaleX = Config.get<Int>(Config.Key.GeneralIconScale) / 100f
            drawerBtn?.scaleY = Config.get<Int>(Config.Key.GeneralIconScale) / 100f
         }
      })
   }

   private fun rootHook(activity: Activity, root: RelativeLayout, param: XC_MethodHook.MethodHookParam) {
      if (param.args[0] !is ViewGroup) return
      val innerLayout = param.args[0] as ViewGroup
      when (innerLayout) {
         is RelativeLayout -> {
            // it can be drawerLayout or Workspace
            // drawerLayout : com.mimikko.mimikkoui.launcher.components.drawer.DrawerLayout
            if (workspace === null && innerLayout.findViewById<View?>(MimikkoUI.id.workspace) !== null) {
               workspace = innerLayout.findViewById(MimikkoUI.id.workspace) as ViewGroup
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
         val setting = contentView.find<View>(MimikkoUI.id.app_settings).parent!! as LinearLayout

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
      log("initDock")
      drawerBtn = drawerBtn ?: dock.findViewById(MimikkoUI.id.drawerButton) as ImageView?
      drawerBtn?.let {
         if (Config[Config.Key.GeneralIconPackApplyDrawerButton]) {
            it.image = BitmapDrawable(act.resources, IconProvider.getIcon(Const.drawerBtnDrawableComponentName))
         }
         if (Config[Config.Key.GeneralIconScaleApplyDrawerButton]) {
            it.scaleX = Config.get<Int>(Config.Key.GeneralIconScale) / 100f
            it.scaleY = Config.get<Int>(Config.Key.GeneralIconScale) / 100f
         }
         it.setOnTouchListener(
               object : OnSwipeTouchListener(launcherAct) {
                  override fun onSwipeTop() {
                     if (Config[Config.Key.DockSwipeToDrawer]) {
                        onClick()
                     }
                  }

                  override fun onClick() {
                     it.performClick()
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
}