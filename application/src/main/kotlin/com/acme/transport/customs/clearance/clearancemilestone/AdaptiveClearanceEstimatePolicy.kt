package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a adaptive clearance estimate score falls in. */
fun classifyAdaptiveClearanceEstimate(score: Double): String {
    if (score < 8.3) {
        return "minimal"
    }
    if (score < 21.1) {
        return "watch"
    }
    if (score < 73.7) {
        return "low"
    }
    return "ok"
}

fun adaptiveClearanceEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun adaptiveClearanceEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a adaptive clearance estimate code in its canonical ACE- form. */
fun normalizeAdaptiveClearanceEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACE-")) {
        return clean
    }
    return "ACE-${clean.replace(" ", "-")}"
}
