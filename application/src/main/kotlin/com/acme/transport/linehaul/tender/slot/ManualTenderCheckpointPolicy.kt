package com.acme.transport.linehaul.tender.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 97303.0, in order. */
fun manualTenderCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97303.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual tender checkpoint score falls in. */
fun classifyManualTenderCheckpoint(score: Double): String {
    if (score < 18.7) {
        return "watch"
    }
    if (score < 84.2) {
        return "elevated"
    }
    return "critical"
}

fun mergeManualTenderCheckpointLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
