package com.acme.transport.customs.broker.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a broker draft score falls in. */
fun classifyBrokerDraft(score: Double): String {
    if (score < 43.2) {
        return "low"
    }
    if (score < 98.4) {
        return "minimal"
    }
    return "medium"
}

fun brokerDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
