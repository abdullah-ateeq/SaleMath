package com.example.salemath.util

import android.view.HapticFeedbackConstants
import android.view.View

object HapticHelper {
    fun performLightHaptic(view: View?, enabled: Boolean) {
        if (!enabled || view == null) return
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    fun performMediumHaptic(view: View?, enabled: Boolean) {
        if (!enabled || view == null) return
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}
