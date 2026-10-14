package com.acme.transport.crewing.restbreak.segment

import com.acme.transport.kit.numbers.roundTo

/** Puts a global rest break snapshot code in its canonical GRBS- form. */
fun normalizeGlobalRestBreakSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GRBS-")) {
        return clean
    }
    return "GRBS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 19541.0, in order. */
fun globalRestBreakSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19541.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun globalRestBreakSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun globalRestBreakSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
