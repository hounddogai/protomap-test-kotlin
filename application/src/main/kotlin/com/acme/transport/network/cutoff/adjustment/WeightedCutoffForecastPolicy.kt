package com.acme.transport.network.cutoff.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedCutoffForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 49320.0, in order. */
fun weightedCutoffForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 49320.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a weighted cutoff forecast score falls in. */
fun classifyWeightedCutoffForecast(score: Double): String {
    if (score < 38.9) {
        return "ok"
    }
    if (score < 63.9) {
        return "medium"
    }
    if (score < 72.1) {
        return "high"
    }
    return "minimal"
}
