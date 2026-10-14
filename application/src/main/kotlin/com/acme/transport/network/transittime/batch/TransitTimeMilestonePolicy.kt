package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a transit time milestone score falls in. */
fun classifyTransitTimeMilestone(score: Double): String {
    if (score < 8.3) {
        return "critical"
    }
    if (score < 18.4) {
        return "ok"
    }
    if (score < 45.9) {
        return "low"
    }
    if (score < 72.0) {
        return "elevated"
    }
    return "high"
}

fun transitTimeMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a transit time milestone code in its canonical TTM- form. */
fun normalizeTransitTimeMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTM-")) {
        return clean
    }
    return "TTM-${clean.replace(" ", "-")}"
}
