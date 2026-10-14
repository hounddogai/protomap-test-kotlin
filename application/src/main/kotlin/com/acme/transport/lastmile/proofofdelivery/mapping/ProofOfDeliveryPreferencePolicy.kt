package com.acme.transport.lastmile.proofofdelivery.mapping

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun proofOfDeliveryPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun proofOfDeliveryPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a proof of delivery preference score falls in. */
fun classifyProofOfDeliveryPreference(score: Double): String {
    if (score < 2.9) {
        return "ok"
    }
    if (score < 37.8) {
        return "low"
    }
    if (score < 93.9) {
        return "medium"
    }
    return "critical"
}
