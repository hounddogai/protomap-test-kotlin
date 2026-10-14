package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun tariffGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.82
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeTariffGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a tariff group score falls in. */
fun classifyTariffGroup(score: Double): String {
    if (score < 28.5) {
        return "critical"
    }
    if (score < 38.2) {
        return "elevated"
    }
    if (score < 66.5) {
        return "ok"
    }
    return "low"
}
