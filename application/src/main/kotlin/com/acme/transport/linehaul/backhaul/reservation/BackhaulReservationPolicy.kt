package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Names the band a backhaul reservation score falls in. */
fun classifyBackhaulReservation(score: Double): String {
    if (score < 14.1) {
        return "low"
    }
    if (score < 78.8) {
        return "minimal"
    }
    if (score < 95.8) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 97291.0, in order. */
fun backhaulReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97291.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeBackhaulReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun backhaulReservationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
