package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun leaseReturnReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun leaseReturnReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a lease return review score falls in. */
fun classifyLeaseReturnReview(score: Double): String {
    if (score < 35.6) {
        return "minimal"
    }
    if (score < 44.6) {
        return "watch"
    }
    return "elevated"
}
