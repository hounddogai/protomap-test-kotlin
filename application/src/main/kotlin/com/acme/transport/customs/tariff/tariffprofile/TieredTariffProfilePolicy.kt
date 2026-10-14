package com.acme.transport.customs.tariff.tariffprofile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a tiered tariff profile score falls in. */
fun classifyTieredTariffProfile(score: Double): String {
    if (score < 33.3) {
        return "minimal"
    }
    if (score < 41.0) {
        return "low"
    }
    if (score < 85.3) {
        return "medium"
    }
    return "ok"
}

fun tieredTariffProfileBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun tieredTariffProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeTieredTariffProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
