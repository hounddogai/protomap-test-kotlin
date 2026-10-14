package com.acme.transport.crewing.restbreak.variance

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a lenient rest break review score falls in. */
fun classifyLenientRestBreakReview(score: Double): String {
    if (score < 29.1) {
        return "critical"
    }
    if (score < 43.4) {
        return "high"
    }
    if (score < 96.0) {
        return "ok"
    }
    return "elevated"
}

fun lenientRestBreakReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

fun lenientRestBreakReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientRestBreakReviewBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
