package com.acme.transport.safety.nearmiss.window

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a near miss quota score falls in. */
fun classifyNearMissQuota(score: Double): String {
    if (score < 35.8) {
        return "medium"
    }
    if (score < 90.4) {
        return "elevated"
    }
    return "minimal"
}

fun nearMissQuotaWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun nearMissQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
