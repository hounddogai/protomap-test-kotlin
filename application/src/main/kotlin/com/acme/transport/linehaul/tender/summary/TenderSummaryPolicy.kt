package com.acme.transport.linehaul.tender.summary

import com.acme.transport.kit.numbers.roundTo

fun tenderSummaryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun tenderSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Puts a tender summary code in its canonical TS- form. */
fun normalizeTenderSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TS-")) {
        return clean
    }
    return "TS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 60929.0, in order. */
fun tenderSummaryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 60929.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
