package com.acme.transport.linehaul.relay.relayrule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun regionalRelayBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Counts how many amounts fit in a budget of 56894.0, in order. */
fun regionalRelayBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 56894.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeRegionalRelayBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a regional relay baseline score falls in. */
fun classifyRegionalRelayBaseline(score: Double): String {
    if (score < 60.3) {
        return "high"
    }
    if (score < 66.2) {
        return "critical"
    }
    if (score < 76.3) {
        return "ok"
    }
    return "medium"
}
