package com.acme.transport.lastmile.drop.digest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 19705.0, in order. */
fun dropDigestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19705.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun dropDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun mergeDropDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a drop digest score falls in. */
fun classifyDropDigest(score: Double): String {
    if (score < 46.1) {
        return "medium"
    }
    if (score < 79.7) {
        return "watch"
    }
    if (score < 81.0) {
        return "critical"
    }
    if (score < 97.5) {
        return "minimal"
    }
    return "low"
}
