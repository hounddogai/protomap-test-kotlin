package com.acme.transport.customs.broker.review

import com.acme.transport.kit.numbers.roundTo

fun seasonalBrokerReviewBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a seasonal broker review score falls in. */
fun classifySeasonalBrokerReview(score: Double): String {
    if (score < 26.2) {
        return "high"
    }
    if (score < 30.0) {
        return "medium"
    }
    if (score < 66.4) {
        return "critical"
    }
    return "low"
}
