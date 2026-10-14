package com.acme.transport.network.hub.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a rolling hub estimate score falls in. */
fun classifyRollingHubEstimate(score: Double): String {
    if (score < 1.7) {
        return "high"
    }
    if (score < 85.8) {
        return "watch"
    }
    return "critical"
}

fun rollingHubEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a rolling hub estimate code in its canonical RHE- form. */
fun normalizeRollingHubEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RHE-")) {
        return clean
    }
    return "RHE-${clean.replace(" ", "-")}"
}

fun rollingHubEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
