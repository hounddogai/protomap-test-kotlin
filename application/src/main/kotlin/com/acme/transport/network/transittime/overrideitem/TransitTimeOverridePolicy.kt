package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 28633.0, in order. */
fun transitTimeOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28633.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun transitTimeOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.77
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a transit time override score falls in. */
fun classifyTransitTimeOverride(score: Double): String {
    if (score < 1.4) {
        return "medium"
    }
    if (score < 3.1) {
        return "elevated"
    }
    if (score < 41.2) {
        return "critical"
    }
    if (score < 61.6) {
        return "low"
    }
    return "high"
}

fun mergeTransitTimeOverrideLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
