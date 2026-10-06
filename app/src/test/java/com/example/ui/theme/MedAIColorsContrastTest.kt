package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/** WCAG 2.x AA for every foreground/background pair the kit uses, in both themes. */
class MedAIColorsContrastTest {

  private fun lin(c: Float): Double = if (c <= 0.03928f) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
  private fun lum(c: Color) = 0.2126 * lin(c.red) + 0.7152 * lin(c.green) + 0.0722 * lin(c.blue)
  private fun over(fg: Color, bg: Color): Color { // fg with alpha drawn over an opaque bg
    val a = fg.alpha
    return Color(fg.red * a + bg.red * (1 - a), fg.green * a + bg.green * (1 - a), fg.blue * a + bg.blue * (1 - a))
  }
  private fun ratio(a: Color, b: Color): Double {
    val la = lum(a); val lb = lum(b)
    return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
  }

  private fun check(theme: String, failures: MutableList<String>, name: String, fg: Color, bg: Color, min: Double) {
    val r = ratio(over(fg, bg), bg)
    if (r < min) failures += "[$theme] $name = ${"%.2f".format(r)} (< $min)"
  }

  private fun run(theme: String, c: MedAIColors) {
    val f = mutableListOf<String>()
    fun text(n: String, fg: Color, bg: Color) = check(theme, f, n, fg, bg, 4.5)
    fun ui(n: String, fg: Color, bg: Color) = check(theme, f, n, fg, bg, 3.0)

    // body text
    for ((n, bg) in listOf("canvas" to c.canvas, "surface" to c.surface, "surfaceSunken" to c.surfaceSunken, "surfaceRaised" to c.surfaceRaised)) {
      text("textPrimary on $n", c.textPrimary, bg)
      text("textSecondary on $n", c.textSecondary, bg)
      text("brand on $n", c.brand, bg)
    }
    text("danger on surface", c.danger, c.surface)
    text("danger on canvas", c.danger, c.canvas)
    text("success on surface", c.success, c.surface)
    text("warning on surface", c.warning, c.surface)
    text("info on surface", c.info, c.surface)
    text("premium on surface", c.premium, c.surface)
    // fills with their label colour
    text("onBrand on brand", c.onBrand, c.brand)
    text("onBrand on brandStrong (pressed)", c.onBrand, c.brandStrong)
    text("onDanger on danger", c.onDanger, c.danger)
    text("onPremium on premium", c.onPremium, c.premium)
    text("brand on brandSoft (pressed text button)", c.brand, c.brandSoft)
    // soft badges
    text("onBrandSoft on brandSoft", c.onBrandSoft, c.brandSoft)
    text("onSuccessSoft on successSoft", c.onSuccessSoft, c.successSoft)
    text("onWarningSoft on warningSoft", c.onWarningSoft, c.warningSoft)
    text("onDangerSoft on dangerSoft", c.onDangerSoft, c.dangerSoft)
    text("onInfoSoft on infoSoft", c.onInfoSoft, c.infoSoft)
    text("onPremiumSoft on premiumSoft", c.onPremiumSoft, c.premiumSoft)
    for ((n, t) in listOf("teal" to c.tintTeal, "peach" to c.tintPeach, "sky" to c.tintSky, "violet" to c.tintViolet)) {
      text("tint $n fg on bg", t.fg, t.bg)
    }
    // hero gradient, plus the translucent metric pills drawn over it
    for ((n, bg) in listOf("heroStart" to c.heroStart, "heroEnd" to c.heroEnd,
      "pill over heroStart" to over(Color.Black.copy(alpha = 0.16f), c.heroStart),
      "pill over heroEnd" to over(Color.Black.copy(alpha = 0.16f), c.heroEnd))) {
      text("onHero on $n", c.onHero, bg)
      text("onHeroMuted on $n", c.onHeroMuted, bg)
    }
    // non-text: input outline, focus ring, error outline
    ui("borderStrong on surface", c.borderStrong, c.surface)
    ui("borderStrong on canvas", c.borderStrong, c.canvas)
    ui("brand focus ring on surface", c.brand, c.surface)
    ui("danger outline on surface", c.danger, c.surface)

    assertTrue("AA failures:\n" + f.joinToString("\n"), f.isEmpty())
  }

  @Test fun light_theme_meets_AA() = run("light", MedAILightColors)
  @Test fun dark_theme_meets_AA() = run("dark", MedAIDarkColors)
}
