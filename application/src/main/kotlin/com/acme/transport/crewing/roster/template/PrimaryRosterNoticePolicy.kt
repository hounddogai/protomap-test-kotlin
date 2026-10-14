package com.acme.transport.crewing.roster.template

import com.acme.transport.kit.numbers.roundTo

/** Puts a primary roster notice code in its canonical PRN- form. */
fun normalizePrimaryRosterNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRN-")) {
        return clean
    }
    return "PRN-${clean.replace(" ", "-")}"
}

fun primaryRosterNoticeBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun primaryRosterNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 84810.0, in order. */
fun primaryRosterNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 84810.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
