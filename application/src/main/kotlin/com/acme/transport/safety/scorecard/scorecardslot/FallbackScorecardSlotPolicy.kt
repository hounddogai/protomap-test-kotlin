package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a fallback scorecard slot code in its canonical FSS- form. */
fun normalizeFallbackScorecardSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSS-")) {
        return clean
    }
    return "FSS-${clean.replace(" ", "-")}"
}

/** Names the band a fallback scorecard slot score falls in. */
fun classifyFallbackScorecardSlot(score: Double): String {
    if (score < 47.2) {
        return "critical"
    }
    if (score < 87.0) {
        return "high"
    }
    return "elevated"
}

fun fallbackScorecardSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun fallbackScorecardSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
