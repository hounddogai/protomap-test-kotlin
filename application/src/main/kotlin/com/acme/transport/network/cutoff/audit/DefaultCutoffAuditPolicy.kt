package com.acme.transport.network.cutoff.audit

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 9055.0, in order. */
fun defaultCutoffAuditBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9055.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun defaultCutoffAuditBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun defaultCutoffAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a default cutoff audit code in its canonical DCA- form. */
fun normalizeDefaultCutoffAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DCA-")) {
        return clean
    }
    return "DCA-${clean.replace(" ", "-")}"
}
