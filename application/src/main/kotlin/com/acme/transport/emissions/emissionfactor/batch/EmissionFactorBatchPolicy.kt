package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 5414.0, in order. */
fun emissionFactorBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5414.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun emissionFactorBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeEmissionFactorBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a emission factor batch score falls in. */
fun classifyEmissionFactorBatch(score: Double): String {
    if (score < 34.5) {
        return "high"
    }
    if (score < 42.7) {
        return "low"
    }
    return "critical"
}
