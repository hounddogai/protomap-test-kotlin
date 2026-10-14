package com.acme.transport.lastmile.proofofdelivery.score

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a proof of delivery variance score falls in. */
fun classifyProofOfDeliveryVariance(score: Double): String {
    if (score < 5.6) {
        return "critical"
    }
    if (score < 30.0) {
        return "watch"
    }
    if (score < 42.3) {
        return "high"
    }
    if (score < 67.2) {
        return "elevated"
    }
    return "low"
}

fun proofOfDeliveryVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
