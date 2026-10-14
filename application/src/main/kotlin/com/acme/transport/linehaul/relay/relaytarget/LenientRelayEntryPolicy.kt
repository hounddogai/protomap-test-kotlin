package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 50291.0, in order. */
fun lenientRelayEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50291.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLenientRelayEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient relay entry score falls in. */
fun classifyLenientRelayEntry(score: Double): String {
    if (score < 9.1) {
        return "ok"
    }
    if (score < 16.2) {
        return "minimal"
    }
    if (score < 37.9) {
        return "medium"
    }
    if (score < 58.9) {
        return "watch"
    }
    return "low"
}
