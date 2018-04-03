package me.manhong2112.mimikkouimod.setting

import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Config
import me.manhong2112.mimikkouimod.common.Const
import me.manhong2112.mimikkouimod.layout.PreferenceLayout
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.editTextPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.seekBarPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.selectorPreference
import me.manhong2112.mimikkouimod.layout.PreferenceLayout.Companion.switchPreference
import me.manhong2112.mimikkouimod.layout.SettingFragment
import me.manhong2112.mimikkouimod.xposed.IconProvider
import java.math.BigInteger

class GeneralSettingFragment : SettingFragment() {
   override fun createView(layout: PreferenceLayout) {
      with(layout) {
         val displayParse: (Float) -> Int = { (it * Const.prefFloatPrecise).toInt() }
         val valueParse: (Int) -> Float = { it / Const.prefFloatPrecise }

         switchPreference(R.string.pref_general_transparent_status_bar, key = Config.Key.GeneralTransparentStatusBar)
         switchPreference(R.string.pref_general_dark_status_bar_icon, key = Config.Key.GeneralDarkStatusBarIcon)

         val iconPacks = IconProvider.getAllIconPack(context)
         selectorPreference(R.string.pref_general_icon_pack, key = Config.Key.GeneralIconPackFallback, displayName = iconPacks.map { it.first }, value = iconPacks.map { listOf(it.second) })
         seekBarPreference<Int>(R.string.pref_general_icon_size, R.string.pref_general_icon_size_num_format, key = Config.Key.GeneralIconScale)

         editTextPreference(R.string.pref_general_icon_text_color,
               key = Config.Key.GeneralShortcutTextColor,
               displayParser = {
                  "%08X".format(it)
               },
               valueParser = {
                  BigInteger(it, 16).toInt()
               })

         val max = 32 * Const.prefFloatPrecise.toInt()
         seekBarPreference(R.string.pref_general_icon_text_size, R.string.pref_general_icon_text_num_format, key = Config.Key.GeneralShortcutTextSize,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
         editTextPreference(R.string.pref_general_icon_text_shadow_color,
               key = Config.Key.GeneralShortcutTextShadowColor,
               displayParser = {
                  "%08X".format(it)
               },
               valueParser = {
                  BigInteger(it, 16).toInt()
               })
         seekBarPreference(R.string.pref_general_icon_text_shadow_radius, R.string.pref_general_icon_text_num_format,
               key = Config.Key.GeneralShortcutTextShadowRadius,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
         seekBarPreference(R.string.pref_general_icon_text_shadow_dx, R.string.pref_general_icon_text_num_format,
               key = Config.Key.GeneralShortcutTextShadowDx,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
         seekBarPreference(R.string.pref_general_icon_text_shadow_dy, R.string.pref_general_icon_text_num_format,
               key = Config.Key.GeneralShortcutTextShadowDy,
               max = max,
               displayParse = displayParse,
               valueParse = valueParse)
      }
   }
}