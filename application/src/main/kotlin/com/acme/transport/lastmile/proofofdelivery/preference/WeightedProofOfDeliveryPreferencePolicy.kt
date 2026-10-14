package com.acme.transport.lastmile.proofofdelivery.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedProofOfDeliveryPreferenceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun weightedProofOfDeliveryPreferenceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 30520.0, in order. */
fun weightedProofOfDeliveryPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30520.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
