package com.acme.transport.network.hub.primaryhubsummary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global hub summary score falls in. */
fun classifyGlobalHubSummary(score: Double): String {
    if (score < 64.6) {
        return "critical"
    }
    if (score < 74.8) {
        return "high"
    }
    return "ok"
}

/** Counts how many amounts fit in a budget of 50445.0, in order. */
fun globalHubSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50445.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeGlobalHubSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
