package com.acme.transport.fleetops.defect.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDefectLedgerLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 74287.0, in order. */
fun defectLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 74287.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a defect ledger score falls in. */
fun classifyDefectLedger(score: Double): String {
    if (score < 10.9) {
        return "minimal"
    }
    if (score < 86.6) {
        return "high"
    }
    if (score < 96.6) {
        return "elevated"
    }
    return "watch"
}

fun defectLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
