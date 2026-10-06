package com.example.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import com.example.ui.*
import com.example.ui.theme.MedAITheme
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Assume
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Screenshots of every MedAI Yordamchi tab and state, light + dark. Needs SHOTS_DIR. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [36], qualifiers = "w411dp-h1500dp-xxhdpi")
class YordamchiShotsTest {
  @get:Rule val rule = createComposeRule()
  private val out = System.getenv("SHOTS_DIR") ?: "screens/current"
  @Before fun gate() { Assume.assumeTrue(System.getenv("SHOTS_DIR") != null) }

  private fun shot(name: String, dark: Boolean, lang: String = "uz", tab: Int = 0, prepare: (AppViewModel) -> Unit = {}, steps: () -> Unit = {}) {
    val vm = SampleData.seededVm(lang)
    prepare(vm)
    rule.setContent {
      MyApplicationTheme(darkTheme = dark) { Box(Modifier.fillMaxSize().background(MedAITheme.colors.canvas)) { MedAIYordamchiScreen(vm, onBack = {}, initialTab = tab) } }
    }
    rule.waitForIdle(); Thread.sleep(800); rule.waitForIdle()
    steps()
    rule.waitForIdle(); Thread.sleep(400); rule.waitForIdle()
    rule.onRoot().captureRoboImage("src/test/screenshots/$out/${name}_${if (dark) "dark" else "light"}.png")
  }

  private fun pill(vm: AppViewModel) {
    setVmFlow(vm, "_pillIdentifyResult", "## Paracetamol\n**Ta'sir:** og'riq qoldiruvchi va isitma tushiruvchi.\n- Kuniga 3-4 martadan ko'p emas\n- Jigar kasalliklarida ehtiyot bo'ling\n1. Ovqatdan keyin\n2. Ko'p suv bilan")
  }

  private fun symptom(vm: AppViewModel) {
    setVmFlow(vm, "_symptomQuestionsData", SymptomQuestionsResult("Bosh og'rig'i", "Cephalalgia", listOf("Og'riq qachondan beri davom etyapti?", "Harorat bormi?", "Ko'ngil aynishi yoki qusish bormi?")))
    setVmFlow(vm, "_symptomDynamicAnalysisResult", "## Ehtimoliy sabablar\n- Charchoq va suvsizlanish\n- Shamollash alomatlari\n**Tavsiya:** dam oling va ko'proq suyuqlik iching.")
  }

  @Test fun pill_light() = shot("yordamchi_pill", false)
  @Test fun pill_dark() = shot("yordamchi_pill", true)
  @Test fun pillResult_light() = shot("yordamchi_pillresult", false, prepare = ::pill)
  @Test fun pillResult_dark() = shot("yordamchi_pillresult", true, prepare = ::pill)
  @Test fun symptom_light() = shot("yordamchi_symptom", false, tab = 1)
  @Test fun symptom_dark() = shot("yordamchi_symptom", true, tab = 1)
  @Test fun symptomResult_light() = shot("yordamchi_symptomresult", false, tab = 1, prepare = ::symptom)
  @Test fun symptomResult_dark() = shot("yordamchi_symptomresult", true, tab = 1, prepare = ::symptom)
  @Test fun stats_light() = shot("yordamchi_stats", false, steps = { rule.onNodeWithText("Statistika").performClick() })
  @Test fun stats_dark() = shot("yordamchi_stats", true, steps = { rule.onNodeWithText("Statistika").performClick() })
  @Test fun reminders_light() = shot("yordamchi_reminders", false, steps = { rule.onNodeWithText("Eslatmalar").performClick() })
  @Test fun reminders_dark() = shot("yordamchi_reminders", true, steps = { rule.onNodeWithText("Eslatmalar").performClick() })
  @Test fun reminderDialog_light() = shot("yordamchi_dialog", false, tab = 3, steps = { rule.onNodeWithText("Yangi eslatma qo'shish").performClick() })
  @Test fun reminderDialog_dark() = shot("yordamchi_dialog", true, tab = 3, steps = { rule.onNodeWithText("Yangi eslatma qo'shish").performClick() })
  @Test fun reminders_ru_light() = shot("yordamchi_reminders_ru", false, lang = "ru", tab = 3)
  @Test fun stats_ru_dark() = shot("yordamchi_stats_ru", true, lang = "ru", tab = 2)
}
