package com.acme.transport.terminal.dwell.dwellreview

import com.acme.transport.kit.numbers.roundTo

fun pendingDwellLedgerBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun pendingDwellLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a pending dwell ledger code in its canonical PDL- form. */
fun normalizePendingDwellLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDL-")) {
        return clean
    }
    return "PDL-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 28921.0, in order. */
fun pendingDwellLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28921.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
