package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a fallback dock door exception score falls in. */
fun classifyFallbackDockDoorException(score: Double): String {
    if (score < 72.8) {
        return "watch"
    }
    if (score < 76.6) {
        return "critical"
    }
    if (score < 86.2) {
        return "elevated"
    }
    return "low"
}

fun mergeFallbackDockDoorExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 78604.0, in order. */
fun fallbackDockDoorExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78604.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
