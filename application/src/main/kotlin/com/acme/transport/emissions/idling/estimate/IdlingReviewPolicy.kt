package com.acme.transport.emissions.idling.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a idling review score falls in. */
fun classifyIdlingReview(score: Double): String {
    if (score < 43.6) {
        return "minimal"
    }
    if (score < 49.1) {
        return "elevated"
    }
    if (score < 69.9) {
        return "ok"
    }
    if (score < 89.8) {
        return "high"
    }
    return "critical"
}

fun idlingReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
