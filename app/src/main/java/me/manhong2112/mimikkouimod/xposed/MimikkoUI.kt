package me.manhong2112.mimikkouimod.xposed

object MimikkoUI {
   const val appVariableName = ""

   const val drawerSetSpanCountMethodName = "setSpanCount"

   const val dockSceneVariableName = "aPH" // in Launcher class
   const val dockLayoutVariableName = "bag" // in DockScene class

   object id {
      const val dock_layout = 0x7f0900ad
      const val drawer_layout = 0x7f0900b4
      const val workspace = 0x7f090318
      const val drawerButton = 0x7f0900b3
      const val bubble = 0x7f090048
      const val app_settings = 0x7f090026

      const val ic_button_drawer = 0x7f08008d
   }

   object dimen {
      const val app_icon_size = 0x7f070055
   }

}