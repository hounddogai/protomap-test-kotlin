package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global service plan digest score falls in. */
fun classifyGlobalServicePlanDigest(score: Double): String {
    if (score < 24.5) {
        return "medium"
    }
    if (score < 50.5) {
        return "minimal"
    }
    return "high"
}

fun mergeGlobalServicePlanDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 17912.0, in order. */
fun globalServicePlanDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17912.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
