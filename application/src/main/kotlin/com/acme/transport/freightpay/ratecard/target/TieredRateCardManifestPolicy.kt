package com.acme.transport.freightpay.ratecard.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun tieredRateCardManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun tieredRateCardManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 61423.0, in order. */
fun tieredRateCardManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61423.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeTieredRateCardManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
