package com.acme.transport.safety.scorecard.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun tieredScorecardEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tiered scorecard entry code in its canonical TSE- form. */
fun normalizeTieredScorecardEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TSE-")) {
        return clean
    }
    return "TSE-${clean.replace(" ", "-")}"
}
