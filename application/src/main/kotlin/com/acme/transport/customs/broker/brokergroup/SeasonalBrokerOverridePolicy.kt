package com.acme.transport.customs.broker.brokergroup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a seasonal broker override score falls in. */
fun classifySeasonalBrokerOverride(score: Double): String {
    if (score < 23.9) {
        return "ok"
    }
    if (score < 69.0) {
        return "high"
    }
    if (score < 89.7) {
        return "critical"
    }
    return "elevated"
}

fun mergeSeasonalBrokerOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun seasonalBrokerOverrideBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun seasonalBrokerOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
