package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a broker rule score falls in. */
fun classifyBrokerRule(score: Double): String {
    if (score < 25.7) {
        return "ok"
    }
    if (score < 33.8) {
        return "watch"
    }
    if (score < 100.0) {
        return "elevated"
    }
    return "critical"
}

fun brokerRuleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.79
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
