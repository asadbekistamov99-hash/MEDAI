package com.example.i18n

import org.junit.Assert.assertEquals
import org.junit.Test

class TranslationsTest {

    @Test
    fun `returns the correct string for each supported language`() {
        assertEquals("Kirish", Translations.getString("login", "uz"))
        assertEquals("Войти", Translations.getString("login", "ru"))
        assertEquals("Login", Translations.getString("login", "en"))
    }

    @Test
    fun `falls back to Uzbek for an unsupported language code`() {
        // "fr" isn't a language the app ships translations for; getString must not crash or
        // return an empty string, it should degrade to the uz copy.
        assertEquals(Translations.getString("login", "uz"), Translations.getString("login", "fr"))
    }

    @Test
    fun `falls back to the key itself for an unknown key`() {
        val unknownKey = "this_key_does_not_exist_in_any_language"
        assertEquals(unknownKey, Translations.getString(unknownKey, "uz"))
        assertEquals(unknownKey, Translations.getString(unknownKey, "en"))
    }

    @Test
    fun `app_slogan is translated, not just copied across languages`() {
        val uz = Translations.getString("app_slogan", "uz")
        val ru = Translations.getString("app_slogan", "ru")
        val en = Translations.getString("app_slogan", "en")

        // Regression guard: it's easy to add a new key to only one language map by mistake
        // (this happened with every string in AdminScreen.kt / MedAIYordamchiScreen.kt, which
        // bypass Translations entirely) — a key that resolves identically in every language
        // usually means at least one language's entry is missing and silently fell back to uz.
        assertEquals("Sizning AI Sog'liqni saqlash yordamchingiz", uz)
        assertEquals("Ваш AI ассистент здоровья", ru)
        assertEquals("Your AI Health Assistant", en)
    }
}
