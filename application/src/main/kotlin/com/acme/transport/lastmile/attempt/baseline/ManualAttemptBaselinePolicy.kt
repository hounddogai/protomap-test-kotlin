package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualAttemptBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual attempt baseline code in its canonical MAB- form. */
fun normalizeManualAttemptBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MAB-")) {
        return clean
    }
    return "MAB-${clean.replace(" ", "-")}"
}

/** Names the band a manual attempt baseline score falls in. */
fun classifyManualAttemptBaseline(score: Double): String {
    if (score < 13.4) {
        return "ok"
    }
    if (score < 90.5) {
        return "high"
    }
    return "elevated"
}
