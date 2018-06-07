package me.manhong2112.mimikkouimod.xposed


import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.common.*
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.common.TypedKey as K


open class GeneralHook {
   private lateinit var classLoader: ClassLoader
   private lateinit var launcherAct: Activity
   private lateinit var app: Application
   private val launcherActCls by lazy {
      XposedHelpers.findClass(MimikkoUI.launcherClsName, classLoader)
   }
   private val bubbleTextViewCls by lazy {
      XposedHelpers.findClass("com.android.launcher3.BubbleTextView", classLoader)
   }

   fun onLoad(clsLoader: ClassLoader, lpparam: XC_LoadPackage.LoadPackageParam) {
      classLoader = clsLoader
      launcherActCls.findMethod("onCreate", Bundle::class.java).hook(after = { param ->
         log("onCreate ${param.args.joinToString(", ")}")
         launcherAct = param.thisObject as Activity
         app = launcherAct.application
         bindConfigUpdateListener()

         IconProvider.update(app)
         hookIcon()
      })

   }

   private fun hookIcon() {
//      val appInfoCls = XposedHelpers.findClass("com.android.launcher3.e", classLoader)
//
//      // appInfoCls.findMethod("a", XposedHelpers.findClass("com.android.launcher3.compat.f", classLoader), java.lang.Integer.TYPE, java.lang.Boolean.TYPE)
//      appInfoCls.hookAllMethods("invalidate") {
//         (it.thisObject).invokeMethod("setIcon", )
//      }

      XposedHelpers.findClass("com.android.launcher3.v", classLoader).findMethod("a",
            XposedHelpers.findClass("com.android.launcher3.compat.f", classLoader), java.lang.Boolean.TYPE)
            .replace {
               IconProvider.getIcon(it.args[0].invokeMethod<ComponentName>("getComponentName").toString())?.let { icon ->
                  return@replace BitmapDrawable(icon)
               }
               throw CallOriginalMethod
            }
      bubbleTextViewCls.declaredConstructors
            .forEach {
               it.hook { p ->
                  val shortcut = p.thisObject as TextView
                  shortcut.setShadowLayer(
                        Config[K.GeneralShortcutTextShadowRadius],
                        Config[K.GeneralShortcutTextShadowDx],
                        Config[K.GeneralShortcutTextShadowDy],
                        Config[K.GeneralShortcutTextShadowColor])
                  shortcut.maxLines = Config[K.GeneralShortcutTextMaxLine]
                  shortcut.setTextColor(Config[K.GeneralShortcutTextColor])
                  shortcut.setTextSize(TypedValue.COMPLEX_UNIT_SP, Config[K.GeneralShortcutTextSize])
                  // shortcut.setField("mIconSize", Config[K.GeneralIconScale] * 32)
               }
            }
   }

   private fun bindConfigUpdateListener() {
      Config.addOnChangeListener(K.GeneralDarkStatusBarIcon, { _, v: Boolean ->
         if (v && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            launcherAct.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
         } else {
            launcherAct.window.decorView.systemUiVisibility = 0
         }
      })
      Config.addOnChangeListener(K.GeneralStatusBarColor) { _, v: Int ->
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val w = launcherAct.window
            w.decorView.fitsSystemWindows = true
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            w.statusBarColor = v
         }
      }
      Config.addOnChangeListener(K.GeneralIconPackFallback) { _, _ ->
         IconProvider.update(app)
      }
   }
}
