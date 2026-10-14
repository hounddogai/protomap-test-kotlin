package com.acme.transport.fleetops.inspection.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun inspectionReviewBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun inspectionReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.53
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a inspection review score falls in. */
fun classifyInspectionReview(score: Double): String {
    if (score < 7.3) {
        return "low"
    }
    if (score < 13.3) {
        return "critical"
    }
    if (score < 13.9) {
        return "elevated"
    }
    if (score < 41.5) {
        return "high"
    }
    return "ok"
}
