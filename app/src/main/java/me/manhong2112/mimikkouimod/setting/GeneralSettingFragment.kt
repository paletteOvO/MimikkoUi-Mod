package me.manhong2112.mimikkouimod.setting

import android.os.Build
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.layout.PreferenceLayout
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.editTextPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.seekBarPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.sortingPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment
import me.manhong2112.mimikkouimod.xposed.IconPackPackageName
import me.manhong2112.mimikkouimod.xposed.IconProvider
import java.math.BigInteger
import me.manhong2112.mimikkouimod.common.TypedKey as K

class GeneralSettingFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         val displayParse: (Float) -> Int = { (it * Const.prefFloatPrecise).toInt() }
         val valueParse: (Int) -> Float = { it / Const.prefFloatPrecise }

         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editTextPreference(R.string.pref_general_status_bar_color,
                  key = K.GeneralStatusBarColor,
                  displayParser = {
                     "%08X".format(it)
                  },
                  valueParser = {
                     BigInteger(it, 16).toInt()
                  })
         } else {
            switchPreference(R.string.pref_general_transparent_status_bar, key = K.GeneralTransparentStatusBar)
         }
         switchPreference(R.string.pref_general_dark_status_bar_icon, key = K.GeneralDarkStatusBarIcon)

         val iconPacks = IconProvider.getAllIconPack(context)
         val configList: List<IconPackPackageName> = Config[K.GeneralIconPackFallback]
         val displayList = (configList.mapNotNull {
            iconPacks.find { item -> item.second == it }?.first
         } + iconPacks.mapNotNull { item ->
            if (item.second !in configList) {
               item.first
            } else {
               null
            }
         }).toMutableList()
         val result = (configList + iconPacks.mapNotNull { item ->
            if (item.second !in configList) {
               item.second
            } else {
               null
            }
         }).toMutableList()

         sortingPreference(R.string.pref_general_icon_pack, K.GeneralIconPackFallback, displayList, result)
         switchPreference(R.string.pref_general_icon_pack_apply_to_drawer_btn, key = K.GeneralIconPackApplyDrawerButton)

         seekBarPreference<Int>(R.string.pref_general_icon_size, R.string.pref_general_icon_size_num_format, key = K.GeneralIconScale)
         switchPreference(R.string.pref_general_icon_size_apply_to_drawer_btn, key = K.GeneralIconScaleApplyDrawerButton)

         switchPreference(R.string.pref_general_icon_text_original_name, key = K.GeneralShortcutTextOriginalName)
         editTextPreference(R.string.pref_general_icon_text_color,
               key = K.GeneralShortcutTextColor,
               displayParser = {
                  "%08X".format(it)
               },
               valueParser = {
                  BigInteger(it, 16).toInt()
               })

         seekBarPreference<Int>(R.string.pref_general_icon_text_max_lines, R.string.pref_general_icon_text_max_lines_num_format, key = K.GeneralShortcutTextMaxLine, max = 3)

         val max = 32 * Const.prefFloatPrecise.toInt()
         seekBarPreference(R.string.pref_general_icon_text_size, R.string.pref_general_icon_text_num_format, key = K.GeneralShortcutTextSize,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
         editTextPreference(R.string.pref_general_icon_text_shadow_color,
               key = K.GeneralShortcutTextShadowColor,
               displayParser = {
                  "%08X".format(it)
               },
               valueParser = {
                  BigInteger(it, 16).toInt()
               })
         seekBarPreference(R.string.pref_general_icon_text_shadow_radius, R.string.pref_general_icon_text_num_format,
               key = K.GeneralShortcutTextShadowRadius,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
         seekBarPreference(R.string.pref_general_icon_text_shadow_dx, R.string.pref_general_icon_text_num_format,
               key = K.GeneralShortcutTextShadowDx,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
         seekBarPreference(R.string.pref_general_icon_text_shadow_dy, R.string.pref_general_icon_text_num_format,
               key = K.GeneralShortcutTextShadowDy,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
      }
   }
}