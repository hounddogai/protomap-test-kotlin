package com.acme.transport.linehaul.trailer.rollup

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun trailerQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.22
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a trailer quota score falls in. */
fun classifyTrailerQuota(score: Double): String {
    if (score < 8.6) {
        return "medium"
    }
    if (score < 22.1) {
        return "critical"
    }
    if (score < 26.2) {
        return "elevated"
    }
    return "low"
}

fun trailerQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
