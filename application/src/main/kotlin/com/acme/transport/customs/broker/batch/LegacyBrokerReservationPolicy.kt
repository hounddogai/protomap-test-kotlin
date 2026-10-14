package com.acme.transport.customs.broker.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun legacyBrokerReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun legacyBrokerReservationBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a legacy broker reservation score falls in. */
fun classifyLegacyBrokerReservation(score: Double): String {
    if (score < 12.3) {
        return "minimal"
    }
    if (score < 53.2) {
        return "ok"
    }
    if (score < 74.9) {
        return "watch"
    }
    return "high"
}

fun mergeLegacyBrokerReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
