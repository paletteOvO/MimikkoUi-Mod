package me.manhong2112.mimikkouimod.xposed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Utils
import me.manhong2112.mimikkouimod.common.Utils.drawableToBitmap
import org.xmlpull.v1.XmlPullParser


object IconProvider {
   private lateinit var ctx: Context
   private lateinit var iconPack: IconPack

   fun update(ctx: Context) {
      Utils.log("IconProvider update")
      this.ctx = ctx
      val value = Config.get<String>(Config.Key.GeneralIconPack)
      if (value == "default") {
         Utils.log("IconProvider update default")
         IconProvider.iconPack = object : IconPack(ctx, "default") {
            override fun getIcon(componentName: ComponentName): Bitmap? {
               return null // IconHook will rollback to original method if icon is null
            }

            override fun hasIcon(componentName: ComponentName): Boolean {
               return true
            }
         }
      } else {
         Utils.log("IconProvider update ${value}")
         IconProvider.iconPack = IconPack(ctx, value)
      }
   }

   fun getIcon(componentName: ComponentName): Bitmap? {
      return iconPack.getIcon(componentName)
   }

   fun hasIcon(componentName: ComponentName): Boolean {
      return iconPack.hasIcon(componentName)
   }

   fun getAllIconPack(ctx: Context): List<Pair<String, String>> {
      val pm = ctx.packageManager
      val list: MutableList<ResolveInfo> = pm.queryIntentActivities(Intent("com.novalauncher.THEME"), 0)
      list.addAll(pm.queryIntentActivities(Intent("org.adw.launcher.icons.ACTION_PICK_ICON"), 0))
      list.addAll(pm.queryIntentActivities(Intent("com.dlto.atom.launcher.THEME"), 0))
      list.addAll(pm.queryIntentActivities(Intent("android.intent.action.MAIN").addCategory("com.anddoes.launcher.THEME"), 0))
      return (list.map {
         val stringId = it.activityInfo.applicationInfo.labelRes
         if (stringId == 0) {
            it.activityInfo.applicationInfo.nonLocalizedLabel.toString()
         } else {
            ctx.packageManager.getResourcesForApplication(it.activityInfo.applicationInfo).getString(stringId)
         } to it.activityInfo.packageName
      }.distinct() + (ctx.getString(R.string.pref_general_none_icon_pack) to "default")).sortedBy { it.first }
   }


   open class IconPack(private val ctx: Context, val name: String) {
      private val icons = HashMap<String, Bitmap>()
      private val appFilter by lazy {
         loadAppFilter()
      }
      private val res by lazy {
         ctx.packageManager.getResourcesForApplication(name)
      }

      open fun getIcon(componentName: ComponentName): Bitmap? {
         val componentInfo = componentName.toString()
         if (componentInfo !in appFilter) {
            return null
         }
         val drawableName = appFilter[componentInfo]!!
         if (drawableName !in icons) {
            val id = res.getIdentifier(drawableName, "drawable", name)
            if (id == 0) {
               return null
            }
            icons[drawableName] = drawableToBitmap(res.getDrawable(id))
         }
         return icons[drawableName]!!
      }

      open fun hasIcon(componentName: ComponentName): Boolean {
         return res.getIdentifier(componentName.toString(), "drawable", name) != 0
      }

      private fun loadAppFilter(): HashMap<String, String> {
         val id = res.getIdentifier("appfilter", "xml", name)
         val xpp = res.getXml(id)
         val hashMap = hashMapOf<String, String>()

         loop@ while (true) {
            val eventType = xpp.next()
            when (eventType) {
               XmlPullParser.START_TAG -> {
                  if (xpp.name == "item") {
                     hashMap[xpp.getAttributeValue(null, "component")] = xpp.getAttributeValue(null, "drawable")
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