package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 34048.0, in order. */
fun lenientTransitTimeRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34048.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient transit time request score falls in. */
fun classifyLenientTransitTimeRequest(score: Double): String {
    if (score < 9.0) {
        return "elevated"
    }
    if (score < 35.8) {
        return "watch"
    }
    if (score < 60.3) {
        return "high"
    }
    if (score < 67.0) {
        return "medium"
    }
    return "critical"
}

fun mergeLenientTransitTimeRequestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
