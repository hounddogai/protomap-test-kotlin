package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun trailerThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.97
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a trailer threshold score falls in. */
fun classifyTrailerThreshold(score: Double): String {
    if (score < 55.5) {
        return "ok"
    }
    if (score < 59.2) {
        return "critical"
    }
    if (score < 66.3) {
        return "high"
    }
    if (score < 78.5) {
        return "elevated"
    }
    return "minimal"
}

fun mergeTrailerThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 88871.0, in order. */
fun trailerThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88871.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
