package com.acme.transport.terminal.traileryard.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 42651.0, in order. */
fun trailerYardMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 42651.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a trailer yard metric score falls in. */
fun classifyTrailerYardMetric(score: Double): String {
    if (score < 29.5) {
        return "minimal"
    }
    if (score < 54.3) {
        return "ok"
    }
    if (score < 93.3) {
        return "medium"
    }
    return "elevated"
}

fun mergeTrailerYardMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun trailerYardMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
