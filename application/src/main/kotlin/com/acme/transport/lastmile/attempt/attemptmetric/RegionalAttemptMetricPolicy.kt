package com.acme.transport.lastmile.attempt.attemptmetric

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun regionalAttemptMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a regional attempt metric score falls in. */
fun classifyRegionalAttemptMetric(score: Double): String {
    if (score < 9.5) {
        return "watch"
    }
    if (score < 41.6) {
        return "high"
    }
    if (score < 58.5) {
        return "ok"
    }
    if (score < 73.7) {
        return "elevated"
    }
    return "medium"
}

fun regionalAttemptMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a regional attempt metric code in its canonical RAM- form. */
fun normalizeRegionalAttemptMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAM-")) {
        return clean
    }
    return "RAM-${clean.replace(" ", "-")}"
}
