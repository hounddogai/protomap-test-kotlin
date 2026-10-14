package com.acme.transport.freightpay.accessorial.rollup

fun batchedAccessorialBaselineSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 54820.0, in order. */
fun batchedAccessorialBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54820.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched accessorial baseline code in its canonical BAB- form. */
fun normalizeBatchedAccessorialBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BAB-")) {
        return clean
    }
    return "BAB-${clean.replace(" ", "-")}"
}
