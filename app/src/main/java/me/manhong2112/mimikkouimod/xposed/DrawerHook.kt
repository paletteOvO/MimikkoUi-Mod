package me.manhong2112.mimikkouimod.xposed

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.OnSwipeTouchListener
import me.manhong2112.mimikkouimod.common.ReflectionUtils.findMethod
import me.manhong2112.mimikkouimod.common.ReflectionUtils.getField
import me.manhong2112.mimikkouimod.common.ReflectionUtils.hook
import me.manhong2112.mimikkouimod.common.ReflectionUtils.invokeMethod
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.ValueBackup
import org.jetbrains.anko.*
import me.manhong2112.mimikkouimod.common.Config.Key as K

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
   private var drawer: ViewGroup? = null // :: RecyclerView
      set(value) {
         field = value
         value?.let {
            initDrawer(launcherAct, it)
         }
      }

   private val wallpaperUpdateReceiver by lazy {
      object : BroadcastReceiver() {
         override fun onReceive(ctx: Context, intent: Intent) {
            updateDrawerBackground(null, null)
         }
      }
   }

   val searchWrap by lazy {
      launcherAct.UI {
         relativeLayout {
            visibility = View.INVISIBLE
            lparams {
               height = dip(48)
               width = matchParent
            }
            editText {
               addTextChangedListener(object : TextWatcher {
                  override fun afterTextChanged(s: Editable) {
                     val adapter = drawer!!.invokeMethod<Any>("getAdapter")
                     adapter.invokeMethod<Unit>("refresh")
                     if (s.isNotEmpty()) {
                        adapter.getField<ArrayList<Any>>("aLN").retainAll {
                           it.invokeMethod<String>("getLabel").contains(s.toString(), true)
                        }
                     }
                     drawer!!.findMethod("setAdapter", XposedHelpers.findClass("android.support.v7.widget.RecyclerView\$Adapter", app.classLoader))
                           .invoke(drawer, adapter)
                  }

                  override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                  override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
               })
            }.lparams {
               height = matchParent
               width = matchParent
            }
         }
      }.view as RelativeLayout
   }

   fun onLoad(classLoader: ClassLoader, lpparam: XC_LoadPackage.LoadPackageParam) {
      this.classLoader = classLoader
      launcherActCls.findMethod("onCreate", Bundle::class.java).hook { param ->
         Utils.log("DrawerHook onCreate ${param.args.joinToString(", ")}")
         launcherAct = param.thisObject as Activity
         app = launcherAct.application

         app.registerReceiver(wallpaperUpdateReceiver, IntentFilter(Intent.ACTION_WALLPAPER_CHANGED)) // well at least it still exist
         bindConfigUpdateListener()
         DrawerBackground.update(launcherAct)
      }

      launcherActCls.findMethod("onDestroy").hook {
         app.unregisterReceiver(wallpaperUpdateReceiver)
      }

      drawerLayoutCls.findMethod("init").hook {
         drawer = it.thisObject as ViewGroup
      }
   }

   private fun updateDrawerBackground(k: K?, v: Any?) {
      DrawerBackground.update(launcherAct, drawer)
   }

   private fun bindConfigUpdateListener() {
      Config.addOnChangeListener(K.DrawerBlurBackground, { k, v: Boolean ->
         updateDrawerBackground(k, v)
         drawer?.let {
            if (v) {
               DrawerBackground.enable(it)
            } else {
               DrawerBackground.disable(it)
            }
         }
      })
      Config.addOnChangeListener(K.DrawerBlurBackgroundBlurRadius, ::updateDrawerBackground)
      Config.addOnChangeListener(K.DrawerDarkBackground, ::updateDrawerBackground)
      Config.addOnChangeListener(K.DrawerColumnSize, { _, v: Int ->
         drawer?.let {
            setDrawerColumnSize(it, v)
         }
      })
      Config.addOnChangeListener(K.DrawerDrawUnderStatusBar) { k, v: Boolean ->
         drawer?.let {
            val lparams = it.layoutParams as RelativeLayout.LayoutParams
            ValueBackup.drawerMarginTop = ValueBackup.drawerMarginTop ?: lparams.topMargin
            if (v) {
               lparams.topMargin = 0
            } else {
               lparams.topMargin = ValueBackup.drawerMarginTop!!
            }
         }
      }

      Config.addOnChangeListener(K.GeneralIconScale, { _, scale: Int ->
         refreshDrawerLayout()
      })
   }

   private fun initDrawer(activity: Activity, drawer: ViewGroup) {
      setDrawerColumnSize(drawer, Config[Config.Key.DrawerColumnSize])
      if (Config[Config.Key.DrawerBlurBackground]) {
         DrawerBackground.enable(drawer)
      }
      if (Config[Config.Key.DrawerDrawUnderStatusBar]) {
         val lparams = drawer.layoutParams as RelativeLayout.LayoutParams
         ValueBackup.drawerMarginTop = ValueBackup.drawerMarginTop ?: lparams.topMargin
         lparams.topMargin = 0
      }
      val h = activity.dip(48)
      val parent = drawer.parent as ViewGroup
      val batView = parent.find<TextView>(MimikkoUI.id.bat)
      parent.find<FrameLayout>(MimikkoUI.id.bat_wrap).addView(searchWrap)
      batView.setOnTouchListener(
            object : OnSwipeTouchListener(activity) {
               override fun onClick() {
                  batView.performClick()
               }

               override fun onSwipeTop() {
                  Utils.log("onSwipeTop")
                  searchWrap.visibility = View.VISIBLE
                  batView.visibility = View.INVISIBLE
                  activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
               }
            }
      )
      XposedHelpers.findAndHookMethod(launcherAct::class.java, "onBackPressed", object : XC_MethodHook() {
         override fun beforeHookedMethod(param: MethodHookParam) {
            if (searchWrap.visibility == View.VISIBLE) {
               drawer.invokeMethod<Any>("getAdapter").invokeMethod<Unit>("refresh")
               searchWrap.visibility = View.INVISIBLE
               batView.visibility = View.VISIBLE
               param.result = null
            }
         }
      })
   }

   private fun setDrawerColumnSize(drawer: ViewGroup, value: Int) {
      drawer.invokeMethod<Any>("getLayoutManager").invokeMethod<Unit>(MimikkoUI.drawerSetSpanCountMethodName, value)
   }

   private fun refreshDrawerLayout() {
      drawer?.let {
         it.findMethod("setAdapter", XposedHelpers.findClass("android.support.v7.widget.RecyclerView\$Adapter", app.classLoader))
               .invoke(it, it.invokeMethod<Any>("getAdapter"))
      }
   }
}