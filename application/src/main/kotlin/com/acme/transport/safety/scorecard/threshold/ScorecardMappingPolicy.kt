package com.acme.transport.safety.scorecard.threshold

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun scorecardMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a scorecard mapping code in its canonical SM- form. */
fun normalizeScorecardMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SM-")) {
        return clean
    }
    return "SM-${clean.replace(" ", "-")}"
}
