package com.acme.transport.customs.hold.segment

import com.acme.transport.kit.numbers.roundTo

/** Puts a hold ledger code in its canonical HL- form. */
fun normalizeHoldLedgerCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HL-")) {
        return clean
    }
    return "HL-${clean.replace(" ", "-")}"
}

fun holdLedgerBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 64423.0, in order. */
fun holdLedgerBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64423.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun holdLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
