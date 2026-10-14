package com.acme.transport.crewing.roster.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun primaryRosterDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a primary roster digest score falls in. */
fun classifyPrimaryRosterDigest(score: Double): String {
    if (score < 51.5) {
        return "critical"
    }
    if (score < 92.2) {
        return "ok"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 61609.0, in order. */
fun primaryRosterDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61609.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePrimaryRosterDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
