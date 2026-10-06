package com.example.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.example.stage3.LayoutChecks
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Profile, Premium upgrade and the three Analytics tabs: no clipped text, 48dp targets, UZ/RU/EN at 411dp and 360dp. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36])
class ProfileGroupLayoutTest {
  @get:Rule val rule = createComposeRule()

  private fun check(where: String, lang: String, tab: String? = null, content: @Composable (AppViewModel) -> Unit) {
    val vm = SampleData.seededVm(lang)
    rule.setContent { MyApplicationTheme { content(vm) } }
    rule.waitForIdle(); Thread.sleep(600); rule.waitForIdle()
    if (tab != null) { rule.onNodeWithText(tab).performClick(); rule.waitForIdle(); Thread.sleep(300); rule.waitForIdle() }
    val clipped = LayoutChecks.clippedTexts(rule)
    val small = LayoutChecks.smallTargets(rule)
    assertTrue("$where: clipped text: $clipped | small targets: $small", clipped.isEmpty() && small.isEmpty())
  }

  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun profile_uz_411() = check("profile/uz/411", "uz") { vm -> ProfileScreen(vm, rememberNavController()) }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun upgrade_uz_411() = check("upgrade/uz/411", "uz") { vm -> PremiumUpgradeScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics0_uz_411() = check("analytics0/uz/411", "uz", tab = "Vazn va BMI") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics1_uz_411() = check("analytics1/uz/411", "uz", tab = "Bosim va puls") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics2_uz_411() = check("analytics2/uz/411", "uz", tab = "Uyqu va ovqat") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun profile_uz_360() = check("profile/uz/360", "uz") { vm -> ProfileScreen(vm, rememberNavController()) }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun upgrade_uz_360() = check("upgrade/uz/360", "uz") { vm -> PremiumUpgradeScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics0_uz_360() = check("analytics0/uz/360", "uz", tab = "Vazn va BMI") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics1_uz_360() = check("analytics1/uz/360", "uz", tab = "Bosim va puls") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics2_uz_360() = check("analytics2/uz/360", "uz", tab = "Uyqu va ovqat") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun profile_ru_411() = check("profile/ru/411", "ru") { vm -> ProfileScreen(vm, rememberNavController()) }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun upgrade_ru_411() = check("upgrade/ru/411", "ru") { vm -> PremiumUpgradeScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics0_ru_411() = check("analytics0/ru/411", "ru", tab = "Вес и ИМТ") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics1_ru_411() = check("analytics1/ru/411", "ru", tab = "Давление и пульс") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics2_ru_411() = check("analytics2/ru/411", "ru", tab = "Сон и питание") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun profile_ru_360() = check("profile/ru/360", "ru") { vm -> ProfileScreen(vm, rememberNavController()) }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun upgrade_ru_360() = check("upgrade/ru/360", "ru") { vm -> PremiumUpgradeScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics0_ru_360() = check("analytics0/ru/360", "ru", tab = "Вес и ИМТ") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics1_ru_360() = check("analytics1/ru/360", "ru", tab = "Давление и пульс") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics2_ru_360() = check("analytics2/ru/360", "ru", tab = "Сон и питание") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun profile_en_411() = check("profile/en/411", "en") { vm -> ProfileScreen(vm, rememberNavController()) }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun upgrade_en_411() = check("upgrade/en/411", "en") { vm -> PremiumUpgradeScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics0_en_411() = check("analytics0/en/411", "en", tab = "Weight & BMI") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics1_en_411() = check("analytics1/en/411", "en", tab = "Pressure & pulse") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w411dp-h2600dp-xxhdpi") @Test fun analytics2_en_411() = check("analytics2/en/411", "en", tab = "Sleep & food") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun profile_en_360() = check("profile/en/360", "en") { vm -> ProfileScreen(vm, rememberNavController()) }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun upgrade_en_360() = check("upgrade/en/360", "en") { vm -> PremiumUpgradeScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics0_en_360() = check("analytics0/en/360", "en", tab = "Weight & BMI") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics1_en_360() = check("analytics1/en/360", "en", tab = "Pressure & pulse") { vm -> AnalyticsScreen(vm) {} }
  @Config(qualifiers = "w360dp-h2600dp-xxhdpi") @Test fun analytics2_en_360() = check("analytics2/en/360", "en", tab = "Sleep & food") { vm -> AnalyticsScreen(vm) {} }
}
