package me.manhong2112.mimikkouimod

import android.content.Context
import android.graphics.Bitmap
import me.manhong2112.mimikkouimod.Utils.drawableToBitmap
import me.manhong2112.mimikkouimod.Utils.log
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

   class IconPack(private val ctx: Context, val packageName: String) {
      private val icons = HashMap<String, Bitmap>()
      private val appFilter by lazy {
         loadAppFilter()
      }
      private val res by lazy {
         ctx.packageManager.getResourcesForApplication(packageName)
      }
      fun getIcon(componentInfo: String): Bitmap? {
         if (componentInfo !in icons) {
            val id = res.getIdentifier(appFilter[componentInfo], "drawable", packageName)
            log("getIcon $componentInfo -> ${appFilter[componentInfo]}")
            if(id == 0) {
               return null
            }
            icons[componentInfo] = drawableToBitmap(res.getDrawable(id))
         }
         return icons[componentInfo]!!
      }

      fun hasIcon(componentInfo: String): Boolean {
         return ctx.resources.getIdentifier(componentInfo, "drawable", packageName) != 0;
      }

      private fun loadAppFilter(): HashMap<String, String> {
         val id = res.getIdentifier("appfilter", "xml", packageName)
         val xpp = res.getXml(id)
         val hashMap = hashMapOf<String, String>()
         var eventType = xpp.eventType
         while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
               XmlPullParser.START_DOCUMENT -> log("Start document")
               XmlPullParser.START_TAG -> {
                  log("Start tag " + xpp.name)
                  if(xpp.name == "item") {
                     hashMap[xpp.getAttributeValue(null, "component")] = xpp.getAttributeValue(null, "drawable")
                  }
               }
               XmlPullParser.END_TAG -> log("End tag " + xpp.name)
               XmlPullParser.TEXT -> log("Text " + xpp.text)
            }
            eventType = xpp.next()
         }
         println("End document")
         return hashMap
      }
   }

}