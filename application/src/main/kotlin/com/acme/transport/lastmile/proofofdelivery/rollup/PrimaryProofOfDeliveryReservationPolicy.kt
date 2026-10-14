package com.acme.transport.lastmile.proofofdelivery.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun primaryProofOfDeliveryReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary proof of delivery reservation score falls in. */
fun classifyPrimaryProofOfDeliveryReservation(score: Double): String {
    if (score < 12.8) {
        return "watch"
    }
    if (score < 85.4) {
        return "high"
    }
    if (score < 87.1) {
        return "ok"
    }
    if (score < 88.6) {
        return "critical"
    }
    return "low"
}

fun primaryProofOfDeliveryReservationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
