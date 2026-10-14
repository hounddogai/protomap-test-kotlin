package com.acme.transport.terminal.crossdock.dockproposal

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 76763.0, in order. */
fun pendingCrossDockNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76763.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun pendingCrossDockNoticeBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a pending cross dock notice code in its canonical PCDN- form. */
fun normalizePendingCrossDockNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCDN-")) {
        return clean
    }
    return "PCDN-${clean.replace(" ", "-")}"
}

fun pendingCrossDockNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
