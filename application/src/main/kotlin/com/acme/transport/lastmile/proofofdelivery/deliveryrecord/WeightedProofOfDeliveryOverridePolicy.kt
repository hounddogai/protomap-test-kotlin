package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a weighted proof of delivery override score falls in. */
fun classifyWeightedProofOfDeliveryOverride(score: Double): String {
    if (score < 23.6) {
        return "ok"
    }
    if (score < 36.4) {
        return "high"
    }
    if (score < 47.2) {
        return "watch"
    }
    return "minimal"
}

fun weightedProofOfDeliveryOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun weightedProofOfDeliveryOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
