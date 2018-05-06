package me.manhong2112.mimikkouimod.xposed

object MimikkoUI {
   const val appVariableName = ""

   const val drawerSetSpanCountMethodName = "setSpanCount"

   const val dockSceneVariableName = "aPH" // in Launcher class
   const val dockLayoutVariableName = "bag" // in DockScene class

   const val packageName = "com.mimikko.mimikkoui"
   const val appClsName = "com.mimikko.common.App"

   const val launcherClsName = "com.mimikko.mimikkoui.launcher.activity.Launcher"
   const val drawerLayoutClsName = "com.mimikko.mimikkoui.launcher.components.drawer.DrawerLayout"
   const val servantSettingClsName = "com.mimikko.servant.activity.ServantSettingsActivity"

   object id {
      var dock_layout = -1 //0x7f0900ad
      var drawer_layout = -1 //0x7f0900b4
      var workspace = -1 //0x7f090318
      var drawerButton = -1 //0x7f0900b3
      var bubble = -1 //0x7f090048
      var app_settings = -1 //0x7f090026
      var bat_bar = -1 //0x7f090038
      var bat = -1 //0x7f090037
      var bat_wrap = -1 //0x7f09003c
   }

   object drawable {
      var ic_button_drawer = -1 //0x7f08008d

   }

   object dimen {
      var app_icon_size = -1 //0x7f070055
   }

}