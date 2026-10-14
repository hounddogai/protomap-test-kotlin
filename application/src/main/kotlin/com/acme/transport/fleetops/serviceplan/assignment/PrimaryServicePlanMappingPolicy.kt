package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 11708.0, in order. */
fun primaryServicePlanMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11708.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePrimaryServicePlanMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a primary service plan mapping score falls in. */
fun classifyPrimaryServicePlanMapping(score: Double): String {
    if (score < 4.4) {
        return "ok"
    }
    if (score < 7.5) {
        return "medium"
    }
    if (score < 33.3) {
        return "minimal"
    }
    if (score < 94.8) {
        return "elevated"
    }
    return "watch"
}
