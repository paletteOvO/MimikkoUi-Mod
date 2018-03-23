package me.manhong2112.mimikkouimod

import android.content.Context
import android.graphics.Bitmap
import me.manhong2112.mimikkouimod.Utils.drawableToBitmap
import org.xmlpull.v1.XmlPullParser

object IconProvider {
   lateinit var iconPack: IconPack
   lateinit var ctx: Context

   fun getIcon(componentInfo: String): Bitmap? {
      return iconPack.getIcon(componentInfo)
   }

   fun hasIcon(componentInfo: String): Boolean {
      return iconPack.hasIcon(componentInfo)
   }

   fun setIconPack(packageName: String) {
      iconPack = IconPack(ctx, packageName)
   }

   class IconPack(private val ctx: Context, val packageName: String) {
      private val icons = HashMap<String, Bitmap>()
      private val appFilter by lazy {
         loadAppFilter()
      }
      private val res by lazy {
         ctx.packageManager.getResourcesForApplication(packageName)
      }

      fun getIcon(componentInfo: String): Bitmap? {
         if (componentInfo !in appFilter) {
            return null
         }
         val drawableName = appFilter[componentInfo]!!
         if (drawableName !in icons) {
            val id = res.getIdentifier(drawableName, "drawable", packageName)
            if (id == 0) {
               return null
            }
            icons[drawableName] = drawableToBitmap(res.getDrawable(id))
         }
         return icons[drawableName]!!
      }

      fun hasIcon(componentInfo: String): Boolean {
         return ctx.resources.getIdentifier(componentInfo, "drawable", packageName) != 0
      }

      private fun loadAppFilter(): HashMap<String, String> {
         val id = res.getIdentifier("appfilter", "xml", packageName)
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
               else -> {}
            }
         }
         return hashMap
      }
   }

}