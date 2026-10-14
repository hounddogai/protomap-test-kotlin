package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a seasonal cutoff assignment score falls in. */
fun classifySeasonalCutoffAssignment(score: Double): String {
    if (score < 39.3) {
        return "critical"
    }
    if (score < 49.2) {
        return "elevated"
    }
    if (score < 60.7) {
        return "medium"
    }
    if (score < 70.2) {
        return "minimal"
    }
    return "ok"
}

fun seasonalCutoffAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.63
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a seasonal cutoff assignment code in its canonical SCA- form. */
fun normalizeSeasonalCutoffAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCA-")) {
        return clean
    }
    return "SCA-${clean.replace(" ", "-")}"
}
