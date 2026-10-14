package com.acme.transport.freightpay.ratecard.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a default rate card entry score falls in. */
fun classifyDefaultRateCardEntry(score: Double): String {
    if (score < 44.1) {
        return "ok"
    }
    if (score < 45.7) {
        return "minimal"
    }
    if (score < 70.4) {
        return "elevated"
    }
    if (score < 99.4) {
        return "low"
    }
    return "watch"
}

fun mergeDefaultRateCardEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 10211.0, in order. */
fun defaultRateCardEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10211.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
