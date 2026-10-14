package com.acme.transport.safety.scorecard.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a scorecard metric score falls in. */
fun classifyScorecardMetric(score: Double): String {
    if (score < 55.3) {
        return "critical"
    }
    if (score < 70.3) {
        return "elevated"
    }
    if (score < 85.8) {
        return "ok"
    }
    return "watch"
}

fun scorecardMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun scorecardMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a scorecard metric code in its canonical SM- form. */
fun normalizeScorecardMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SM-")) {
        return clean
    }
    return "SM-${clean.replace(" ", "-")}"
}
