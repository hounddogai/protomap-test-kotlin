package com.acme.transport.lastmile.attempt.attemptdraft

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a seasonal attempt entry code in its canonical SAE- form. */
fun normalizeSeasonalAttemptEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAE-")) {
        return clean
    }
    return "SAE-${clean.replace(" ", "-")}"
}

fun seasonalAttemptEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.58
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
