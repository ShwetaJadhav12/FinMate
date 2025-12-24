package com.example.finmate.components

import android.content.Context
import androidx.core.content.edit

object OnboardingPrefs {

    private const val PREF_NAME = "finmate_prefs"
    private const val KEY_SHOW = "show_onboarding"

    fun shouldShow(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SHOW, false)
    }

    fun enable(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHOW, true) }
    }

    fun disable(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_SHOW, false) }
    }
}


