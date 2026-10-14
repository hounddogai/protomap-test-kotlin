package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeRollingFuelSurchargePreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun rollingFuelSurchargePreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.09
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a rolling fuel surcharge preference score falls in. */
fun classifyRollingFuelSurchargePreference(score: Double): String {
    if (score < 40.1) {
        return "watch"
    }
    if (score < 50.4) {
        return "minimal"
    }
    if (score < 54.1) {
        return "low"
    }
    if (score < 55.8) {
        return "medium"
    }
    return "elevated"
}
