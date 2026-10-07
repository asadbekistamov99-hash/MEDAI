package com.example.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.stage3.LayoutChecks
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Sets a private MutableStateFlow of the view model (the AI results cannot be produced offline). */
@Suppress("UNCHECKED_CAST")
internal fun setVmFlow(vm: AppViewModel, field: String, value: Any?) {
  val f = AppViewModel::class.java.getDeclaredField(field)
  f.isAccessible = true
  (f.get(vm) as MutableStateFlow<Any?>).value = value
}

internal val YordamchiTabLabels = mapOf(
  "uz" to listOf("Dori aniqlash", "Simptomlar", "Statistika", "Eslatmalar"),
  "ru" to listOf("Лекарства", "Симптомы", "Статистика", "График приёма"),
  "en" to listOf("Pill ID", "Symptoms", "Statistics", "Reminders"),
)

internal val YordamchiAddLabel = mapOf("uz" to "Yangi eslatma qo'shish", "ru" to "Добавить напоминание", "en" to "Add reminder")

/** Every MedAI Yordamchi tab (and its result / dialog states): no clipped text, 48dp targets, UZ/RU/EN. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
abstract class YordamchiLayoutBase {
  @get:Rule val rule = createComposeRule()

  private fun check(where: String, lang: String, tab: Int, prepare: (AppViewModel) -> Unit = {}, steps: () -> Unit = {}) {
    val vm = SampleData.seededVm(lang)
    prepare(vm)
    rule.setContent { MyApplicationTheme { MedAIYordamchiScreen(vm, onBack = {}, initialTab = tab) } }
    rule.waitForIdle(); Thread.sleep(600); rule.waitForIdle()
    steps()
    rule.waitForIdle(); Thread.sleep(300); rule.waitForIdle()
    val clipped = LayoutChecks.clippedTexts(rule)
    val small = LayoutChecks.smallTargets(rule)
    assertTrue("$where: clipped text: $clipped | small targets: $small", clipped.isEmpty() && small.isEmpty())
  }

  private fun pill(vm: AppViewModel) {
    setVmFlow(vm, "_pillIdentifyResult", "## Paracetamol\n**Ta'sir:** og'riq qoldiruvchi va isitma tushiruvchi.\n- Kuniga 3-4 martadan ko'p emas\n- Jigar kasalliklarida ehtiyot bo'ling\n1. Ovqatdan keyin\n2. Ko'p suv bilan")
  }

  private fun symptom(vm: AppViewModel) {
    setVmFlow(vm, "_symptomQuestionsData", SymptomQuestionsResult("Bosh og'rig'i", "Cephalalgia", listOf("Og'riq qachondan beri davom etyapti?", "Harorat bormi?", "Ko'ngil aynishi yoki qusish bormi?")))
    setVmFlow(vm, "_symptomDynamicAnalysisResult", "## Ehtimoliy sabablar\n- Charchoq va suvsizlanish\n- Shamollash alomatlari\n**Tavsiya:** dam oling va ko'proq suyuqlik iching.")
  }

  private fun t(lang: String, i: Int) = YordamchiTabLabels.getValue(lang)[i]

  // Tabs as opened from the tab bar
  @Test fun pill_uz() = check("pill/uz", "uz", 0)
  @Test fun pill_ru() = check("pill/ru", "ru", 0)
  @Test fun pill_en() = check("pill/en", "en", 0)
  @Test fun pillResult_uz() = check("pillResult/uz", "uz", 0, ::pill)
  @Test fun pillResult_ru() = check("pillResult/ru", "ru", 0, ::pill)
  @Test fun pillResult_en() = check("pillResult/en", "en", 0, ::pill)
  @Test fun symptom_uz() = check("symptom/uz", "uz", 0, steps = { rule.onNodeWithText(t("uz", 1)).performClick() })
  @Test fun symptom_ru() = check("symptom/ru", "ru", 0, steps = { rule.onNodeWithText(t("ru", 1)).performClick() })
  @Test fun symptom_en() = check("symptom/en", "en", 0, steps = { rule.onNodeWithText(t("en", 1)).performClick() })
  @Test fun symptomResult_uz() = check("symptomResult/uz", "uz", 1, ::symptom)
  @Test fun symptomResult_ru() = check("symptomResult/ru", "ru", 1, ::symptom)
  @Test fun symptomResult_en() = check("symptomResult/en", "en", 1, ::symptom)
  @Test fun stats_uz() = check("stats/uz", "uz", 0, steps = { rule.onNodeWithText(t("uz", 2)).performClick() })
  @Test fun stats_ru() = check("stats/ru", "ru", 0, steps = { rule.onNodeWithText(t("ru", 2)).performClick() })
  @Test fun stats_en() = check("stats/en", "en", 0, steps = { rule.onNodeWithText(t("en", 2)).performClick() })
  @Test fun reminders_uz() = check("reminders/uz", "uz", 0, steps = { rule.onNodeWithText(t("uz", 3)).performClick() })
  @Test fun reminders_ru() = check("reminders/ru", "ru", 0, steps = { rule.onNodeWithText(t("ru", 3)).performClick() })
  @Test fun reminders_en() = check("reminders/en", "en", 0, steps = { rule.onNodeWithText(t("en", 3)).performClick() })
  @Test fun reminderDialog_uz() = check("reminderDialog/uz", "uz", 3, steps = { rule.onNodeWithText(YordamchiAddLabel.getValue("uz")).performClick() })
  @Test fun reminderDialog_ru() = check("reminderDialog/ru", "ru", 3, steps = { rule.onNodeWithText(YordamchiAddLabel.getValue("ru")).performClick() })
  @Test fun reminderDialog_en() = check("reminderDialog/en", "en", 3, steps = { rule.onNodeWithText(YordamchiAddLabel.getValue("en")).performClick() })
}

@Config(sdk = [36], qualifiers = "w411dp-h2600dp-xxhdpi")
class YordamchiLayoutTest : YordamchiLayoutBase()

@Config(sdk = [36], qualifiers = "w360dp-h2600dp-xxhdpi")
class YordamchiLayout360Test : YordamchiLayoutBase()
