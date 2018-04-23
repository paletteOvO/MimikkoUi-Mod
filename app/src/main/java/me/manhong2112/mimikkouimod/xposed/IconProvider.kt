package me.manhong2112.mimikkouimod.xposed

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.util.ArrayMap
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.arrayMapOf
import me.manhong2112.mimikkouimod.common.Utils.log
import me.manhong2112.mimikkouimod.common.Utils.toBitmap
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.lang.ref.WeakReference

typealias IconPackName = String
typealias IconPackPackageName = String

object IconProvider {
   private lateinit var ctx: WeakReference<Context>
   private lateinit var iconPacks: List<IconPack>
   private val iconPacksCache: ArrayMap<String, IconPack> by lazy {
      arrayMapOf<String, IconPack>(
            "default" to DefaultIconPack
      )
   }

   object DefaultIconPack : IconPack(ctx, "default") {
      override fun getIcon(componentName: String): Bitmap? {
         if (componentName == Const.drawerBtnDrawableComponentName) {
            return ctx.get()?.let {
               ContextCompat.getDrawable(it, MimikkoUI.drawable.ic_button_drawer)?.toBitmap()
            }
         }
         return null // IconHook will rollback to original method if icon is null
      }

      override fun hasIcon(componentName: String): Boolean {
         return true
      }
   }

   private fun <K, V> Map<K, V>.foreach(callback: (K, V) -> Unit) {
      this.entries.forEach {
         callback(it.key, it.value)
      }
   }

   fun update(ctx: Context) {
      Utils.log("IconProvider update")
      this.ctx = WeakReference(ctx)
      val value = Config.get<List<String>>(Config.Key.GeneralIconPackFallback) + "default"
      iconPacks = value.map {
         log("loading iconpack $it")
         iconPacksCache[it] = iconPacksCache[it] ?: IconPack(WeakReference(ctx), it)
         iconPacksCache[it]!!
      }
      iconPacksCache.foreach { k, _ ->
         if (k !in value) {
            iconPacksCache.remove(k)
         }
      }

   }

   fun getIcon(componentName: String): Bitmap? {
      iconPacks.forEach {
         if (it.hasIcon(componentName)) {
            // log("getIcon from $it")
            return it.getIcon(componentName)
         }
      }
      return null
   }

   fun hasIcon(componentName: String): Boolean {
      iconPacks.forEach {
         if (it.hasIcon(componentName)) {
            return true
         }
      }
      return false
   }

   fun getAllIconPack(ctx: Context): List<Pair<IconPackName, IconPackPackageName>> {
      val pm = ctx.packageManager

      val list: MutableList<ResolveInfo> = pm.queryIntentActivities(Intent("com.novalauncher.THEME"), 0) // nova
      list.addAll(pm.queryIntentActivities(Intent("android.intent.action.MAIN").addCategory("com.teslacoilsw.launcher.THEME"), 0)) // nova
      list.addAll(pm.queryIntentActivities(Intent("org.adw.launcher.icons.ACTION_PICK_ICON"), 0)) // adw
      list.addAll(pm.queryIntentActivities(Intent("com.dlto.atom.launcher.THEME"), 0)) // atom
      list.addAll(pm.queryIntentActivities(Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME"), 0)) // Apex
      return (list.map {
         val stringId = it.activityInfo.applicationInfo.labelRes
         if (stringId == 0) {
            it.activityInfo.applicationInfo.nonLocalizedLabel.toString()
         } else {
            ctx.packageManager.getResourcesForApplication(it.activityInfo.applicationInfo).getString(stringId)
         } to it.activityInfo.packageName
      }.distinct() + (ctx.getString(R.string.pref_general_none_icon_pack) to "default")).sortedBy { it.first }
   }


   open class IconPack(protected val ctx: WeakReference<Context>, protected val packageName: String) {
      private val icons = HashMap<String, Bitmap>()
      private val appFilter by lazy {
         loadAppFilter()
      }
      protected val res by lazy {
         ctx.get()?.packageManager?.getResourcesForApplication(packageName)
      }

      override fun toString(): String {
         return "IconPack($packageName)"
      }

      open fun getIcon(componentName: String): Bitmap? {
         if (componentName !in appFilter) {
            return null
         }
         val drawableName = appFilter[componentName]!!
         if (drawableName !in icons) {
            res?.let { res ->
               // log("load drawable $drawableName")
               val id = res.getIdentifier(drawableName, "drawable", packageName)
               if (id == 0) {
                  log("failed to get drawable $drawableName from $packageName")
                  return null
               }
               // log("cache drawable")
               icons[drawableName] = res.getDrawable(id)!!.toBitmap()
            }
         }
         return icons[drawableName]!!
      }

      open fun hasIcon(componentName: String): Boolean {
         return (componentName in appFilter).also {
            // log("$packageName has icon $componentName == $it")
         }
      }

      private fun loadAppFilter(): HashMap<String, String> {
         val hashMap = hashMapOf<String, String>()
         res ?: run {
            log("res is null")
            return hashMap
         }
         val id = res!!.getIdentifier("appfilter", "xml", packageName)
         val parser = if (id != 0) {
            res!!.getXml(id)
         } else {
            ctx.get() ?: return hashMap
            val otherContext = ctx.get()!!.createPackageContext(packageName, 0)
            val am = otherContext.assets
            val f = XmlPullParserFactory.newInstance()
            f.isNamespaceAware = true
            f.newPullParser().also {
               it.setInput(am.open("appfilter.xml"), "utf-8")
            }
         }

         loop@ while (true) {
            val eventType = parser.next()
            when (eventType) {
               XmlPullParser.START_TAG -> {
                  if (parser.name == "item") {
                     val key = parser.getAttributeValue(null, "component") ?: continue@loop
                     val value: String = parser.getAttributeValue(null, "drawable") ?: continue@loop
                     log("$key -> $value")
                     hashMap[key] = value
                  }
               }
               XmlPullParser.END_DOCUMENT -> break@loop
               else -> {
               }
            }
         }
         return hashMap
      }
   }

}