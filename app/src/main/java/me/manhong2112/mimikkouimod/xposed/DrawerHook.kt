package me.manhong2112.mimikkouimod.xposed

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.ViewGroup
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.common.*
import me.manhong2112.mimikkouimod.common.TypedKey as K


class DrawerHook {
   private lateinit var classLoader: ClassLoader
   private lateinit var launcherAct: Activity
   private lateinit var app: Application
   private val launcherActCls by lazy {
      XposedHelpers.findClass(MimikkoUI.launcherClsName, classLoader)
   }
   private val drawerLayoutCls by lazy {
      XposedHelpers.findClass(MimikkoUI.drawerLayoutClsName, classLoader)
   }
   private val drawerCellCls by lazy {
      XposedHelpers.findClass("com.mimikko.mimikkoui.launcher3.customization.allapps.PlainCellLayout", classLoader)
   }

   private var drawer: ViewGroup? = null // :: RecyclerView

   fun onLoad(classLoader: ClassLoader, lpparam: XC_LoadPackage.LoadPackageParam) {
      this.classLoader = classLoader
      launcherActCls.findMethod("onCreate", Bundle::class.java).hook { param ->
         Utils.log("DrawerHook onCreate ${param.args.joinToString(", ")}")
         launcherAct = param.thisObject as Activity
         app = launcherAct.application

         bindConfigUpdateListener()
      }
      drawerLayoutCls.getConstructor(
            Context::class.java,
            AttributeSet::class.java,
            java.lang.Integer.TYPE
      ).hook {
         drawer = it.thisObject as ViewGroup
      }

      drawerCellCls.hookAllMethods("onLayout", before = {
         it.thisObject.invokeMethod("setRow", Config[K.DrawerColumnSize])
      })
   }

   private fun bindConfigUpdateListener() {
      arrayOf(
            K.DrawerColumnSize,
            K.GeneralIconScale,
            K.GeneralShortcutTextShadowRadius,
            K.GeneralShortcutTextShadowDx,
            K.GeneralShortcutTextShadowDy,
            K.GeneralShortcutTextShadowColor,
            K.GeneralShortcutTextMaxLine,
            K.GeneralShortcutTextColor,
            K.GeneralShortcutTextSize).forEach {
         Config.addOnChangeListener(it) { _, _ ->
            refreshDrawerLayout()
         }
      }
   }

   private fun refreshDrawerLayout() {
      drawer?.let {
         it.findMethod("setAdapter", XposedHelpers.findClass("android.support.v7.widget.RecyclerView\$Adapter", app.classLoader))
               .invoke(it, it.invokeMethod<Any>("getAdapter"))
      }
   }
}