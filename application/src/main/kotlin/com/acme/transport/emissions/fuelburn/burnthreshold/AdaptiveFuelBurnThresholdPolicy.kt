package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 20354.0, in order. */
fun adaptiveFuelBurnThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20354.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeAdaptiveFuelBurnThresholdLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
