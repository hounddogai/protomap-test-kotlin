package com.acme.transport.network.zone.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 35332.0, in order. */
fun zoneMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 35332.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a zone metric score falls in. */
fun classifyZoneMetric(score: Double): String {
    if (score < 11.0) {
        return "ok"
    }
    if (score < 27.5) {
        return "high"
    }
    if (score < 51.7) {
        return "elevated"
    }
    if (score < 80.5) {
        return "watch"
    }
    return "low"
}

fun zoneMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeZoneMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
