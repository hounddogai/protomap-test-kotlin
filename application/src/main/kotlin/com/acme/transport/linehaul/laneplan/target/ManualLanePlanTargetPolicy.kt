package com.acme.transport.linehaul.laneplan.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a manual lane plan target score falls in. */
fun classifyManualLanePlanTarget(score: Double): String {
    if (score < 56.6) {
        return "critical"
    }
    if (score < 96.8) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a manual lane plan target code in its canonical MLPT- form. */
fun normalizeManualLanePlanTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MLPT-")) {
        return clean
    }
    return "MLPT-${clean.replace(" ", "-")}"
}

fun manualLanePlanTargetBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun manualLanePlanTargetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
