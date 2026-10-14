package com.acme.transport.linehaul.laneplan.planreservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 30400.0, in order. */
fun adaptiveLanePlanBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 30400.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveLanePlanBundleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun mergeAdaptiveLanePlanBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
