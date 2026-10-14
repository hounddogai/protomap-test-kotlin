package com.acme.transport.customs.broker.baseline

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedBrokerRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a capped broker record score falls in. */
fun classifyCappedBrokerRecord(score: Double): String {
    if (score < 12.7) {
        return "medium"
    }
    if (score < 50.1) {
        return "critical"
    }
    return "ok"
}
