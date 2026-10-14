package com.acme.transport.network.transittime.proposal

import com.acme.transport.kit.numbers.roundTo

fun globalTransitTimeLedgerBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun globalTransitTimeLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a global transit time ledger code in its canonical GTTL- form. */
fun normalizeGlobalTransitTimeLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTTL-")) {
        return clean
    }
    return "GTTL-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 37702.0, in order. */
fun globalTransitTimeLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37702.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
