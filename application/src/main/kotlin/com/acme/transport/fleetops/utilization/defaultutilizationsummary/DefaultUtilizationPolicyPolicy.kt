package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun defaultUtilizationPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a default utilization policy score falls in. */
fun classifyDefaultUtilizationPolicy(score: Double): String {
    if (score < 39.4) {
        return "high"
    }
    if (score < 67.6) {
        return "ok"
    }
    if (score < 72.2) {
        return "elevated"
    }
    if (score < 87.2) {
        return "medium"
    }
    return "low"
}

fun mergeDefaultUtilizationPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 80322.0, in order. */
fun defaultUtilizationPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80322.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
