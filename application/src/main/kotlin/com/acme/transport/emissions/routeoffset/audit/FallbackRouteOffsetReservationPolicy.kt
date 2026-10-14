package com.acme.transport.emissions.routeoffset.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 67915.0, in order. */
fun fallbackRouteOffsetReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 67915.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeFallbackRouteOffsetReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
