package com.acme.transport.crewing.availability.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 70217.0, in order. */
fun primaryAvailabilityThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 70217.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryAvailabilityThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.74
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergePrimaryAvailabilityThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a primary availability threshold score falls in. */
fun classifyPrimaryAvailabilityThreshold(score: Double): String {
    if (score < 34.2) {
        return "elevated"
    }
    if (score < 96.9) {
        return "low"
    }
    return "minimal"
}
