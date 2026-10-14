package com.acme.transport.linehaul.relay.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 31658.0, in order. */
fun seasonalRelayMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31658.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a seasonal relay milestone code in its canonical SRM- form. */
fun normalizeSeasonalRelayMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SRM-")) {
        return clean
    }
    return "SRM-${clean.replace(" ", "-")}"
}

fun mergeSeasonalRelayMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun seasonalRelayMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
