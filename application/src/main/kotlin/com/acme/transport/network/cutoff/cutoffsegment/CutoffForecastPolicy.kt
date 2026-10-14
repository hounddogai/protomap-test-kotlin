package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun cutoffForecastBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a cutoff forecast score falls in. */
fun classifyCutoffForecast(score: Double): String {
    if (score < 15.6) {
        return "critical"
    }
    if (score < 28.7) {
        return "ok"
    }
    if (score < 81.8) {
        return "minimal"
    }
    if (score < 88.8) {
        return "medium"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 17169.0, in order. */
fun cutoffForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17169.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeCutoffForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
