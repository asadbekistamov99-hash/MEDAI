package com.example.ui

/** Picks the uz / ru / en variant of a string for the support screens (History, AI Chat, Reminders, SOS). */
internal fun supportText(lang: String, uz: String, ru: String, en: String): String = when (lang) {
    "uz" -> uz
    "ru" -> ru
    else -> en
}
