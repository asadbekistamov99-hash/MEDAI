package com.example.catalog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performSemanticsAction
import com.example.stage3.LayoutChecks
import com.example.ui.AdminScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Every Admin tab (and the CMS / log sub-tabs): no clipped text, 48dp targets, UZ/RU/EN at 411dp and 360dp. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class AdminLayoutTest {
  @get:Rule val rule = createComposeRule()

  private val tabNames = mapOf(
    "uz" to listOf("Foydalanuvchilar", "To'lovlar", "Tizim", "Tibbiy CMS", "Jurnallar"),
    "ru" to listOf("Пользователи", "Платежи", "Система", "Мед. CMS", "Журналы"),
    "en" to listOf("Users", "Payments", "System", "Medical CMS", "Logs"),
  )
  // Sub-tab chips: CMS (diseases is default) -> medicines, tips; logs -> errors.
  private val cmsSub = mapOf("uz" to listOf("Dorilar", "Maslahatlar"), "ru" to listOf("Лекарства", "Советы"), "en" to listOf("Medicines", "Tips"))
  private val errSub = mapOf("uz" to "Xatolar (", "ru" to "Ошибки (", "en" to "Errors (")

  private fun check(where: String, lang: String, tab: Int, sub: String? = null, subIndex: Int = 0) {
    val vm = AdminTestData.adminVm(lang)
    rule.setContent { MyApplicationTheme { AdminScreen(vm) {} } }
    rule.waitForIdle(); Thread.sleep(600); rule.waitForIdle()
    fun click(text: String, index: Int = 0) {
      rule.onAllNodesWithText(text, substring = text.endsWith("("))[index].performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.OnClick)
      rule.waitForIdle(); Thread.sleep(300); rule.waitForIdle()
    }
    click(tabNames.getValue(lang)[tab])
    // The CMS tiles repeat the sub-tab names, so the chip is the LAST match.
    if (sub != null) { val n = rule.onAllNodesWithText(sub, substring = sub.endsWith("(")).fetchSemanticsNodes().size; click(sub, if (subIndex < 0) n - 1 else subIndex) }
    val clipped = LayoutChecks.clippedTexts(rule)
    val small = LayoutChecks.smallTargets(rule)
    assertTrue("$where: clipped text: $clipped | small targets: $small", clipped.isEmpty() && small.isEmpty())
  }

  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun users_uz_411() = check("users/uz/411", "uz", 0)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun payments_uz_411() = check("payments/uz/411", "uz", 1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun system_uz_411() = check("system/uz/411", "uz", 2)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_uz_411() = check("cms/uz/411", "uz", 3)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun logs_uz_411() = check("logs/uz/411", "uz", 4)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_sub0_uz_411() = check("cms-sub0/uz/411", "uz", 3, cmsSub.getValue("uz")[0], -1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_sub1_uz_411() = check("cms-sub1/uz/411", "uz", 3, cmsSub.getValue("uz")[1], -1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun errors_uz_411() = check("errors/uz/411", "uz", 4, errSub.getValue("uz"))
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun users_uz_360() = check("users/uz/360", "uz", 0)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun payments_uz_360() = check("payments/uz/360", "uz", 1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun system_uz_360() = check("system/uz/360", "uz", 2)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_uz_360() = check("cms/uz/360", "uz", 3)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun logs_uz_360() = check("logs/uz/360", "uz", 4)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_sub0_uz_360() = check("cms-sub0/uz/360", "uz", 3, cmsSub.getValue("uz")[0], -1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_sub1_uz_360() = check("cms-sub1/uz/360", "uz", 3, cmsSub.getValue("uz")[1], -1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun errors_uz_360() = check("errors/uz/360", "uz", 4, errSub.getValue("uz"))
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun users_ru_411() = check("users/ru/411", "ru", 0)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun payments_ru_411() = check("payments/ru/411", "ru", 1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun system_ru_411() = check("system/ru/411", "ru", 2)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_ru_411() = check("cms/ru/411", "ru", 3)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun logs_ru_411() = check("logs/ru/411", "ru", 4)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_sub0_ru_411() = check("cms-sub0/ru/411", "ru", 3, cmsSub.getValue("ru")[0], -1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_sub1_ru_411() = check("cms-sub1/ru/411", "ru", 3, cmsSub.getValue("ru")[1], -1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun errors_ru_411() = check("errors/ru/411", "ru", 4, errSub.getValue("ru"))
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun users_ru_360() = check("users/ru/360", "ru", 0)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun payments_ru_360() = check("payments/ru/360", "ru", 1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun system_ru_360() = check("system/ru/360", "ru", 2)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_ru_360() = check("cms/ru/360", "ru", 3)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun logs_ru_360() = check("logs/ru/360", "ru", 4)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_sub0_ru_360() = check("cms-sub0/ru/360", "ru", 3, cmsSub.getValue("ru")[0], -1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_sub1_ru_360() = check("cms-sub1/ru/360", "ru", 3, cmsSub.getValue("ru")[1], -1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun errors_ru_360() = check("errors/ru/360", "ru", 4, errSub.getValue("ru"))
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun users_en_411() = check("users/en/411", "en", 0)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun payments_en_411() = check("payments/en/411", "en", 1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun system_en_411() = check("system/en/411", "en", 2)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_en_411() = check("cms/en/411", "en", 3)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun logs_en_411() = check("logs/en/411", "en", 4)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_sub0_en_411() = check("cms-sub0/en/411", "en", 3, cmsSub.getValue("en")[0], -1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun cms_sub1_en_411() = check("cms-sub1/en/411", "en", 3, cmsSub.getValue("en")[1], -1)
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun errors_en_411() = check("errors/en/411", "en", 4, errSub.getValue("en"))
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun users_en_360() = check("users/en/360", "en", 0)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun payments_en_360() = check("payments/en/360", "en", 1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun system_en_360() = check("system/en/360", "en", 2)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_en_360() = check("cms/en/360", "en", 3)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun logs_en_360() = check("logs/en/360", "en", 4)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_sub0_en_360() = check("cms-sub0/en/360", "en", 3, cmsSub.getValue("en")[0], -1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun cms_sub1_en_360() = check("cms-sub1/en/360", "en", 3, cmsSub.getValue("en")[1], -1)
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun errors_en_360() = check("errors/en/360", "en", 4, errSub.getValue("en"))
}
