package com.acme.transport.customs.broker.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun brokerScoreBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a broker score score falls in. */
fun classifyBrokerScore(score: Double): String {
    if (score < 53.9) {
        return "minimal"
    }
    if (score < 73.3) {
        return "watch"
    }
    return "medium"
}

fun brokerScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
