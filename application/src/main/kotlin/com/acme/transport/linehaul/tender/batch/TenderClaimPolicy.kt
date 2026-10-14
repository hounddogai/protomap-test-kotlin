package com.acme.transport.linehaul.tender.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun tenderClaimBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a tender claim score falls in. */
fun classifyTenderClaim(score: Double): String {
    if (score < 27.3) {
        return "watch"
    }
    if (score < 71.4) {
        return "high"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 32892.0, in order. */
fun tenderClaimBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32892.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeTenderClaimLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
