package com.example.finmate.components

import android.content.Context
import androidx.core.content.edit

object OnboardingPrefs {

    private const val PREF_NAME = "finmate_prefs"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"

    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false)
    }

    fun setOnboardingCompleted(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ONBOARDING_DONE, true)
        }
    }

    fun reset(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putBoolean(KEY_ONBOARDING_DONE, false)
        }
    }
}
