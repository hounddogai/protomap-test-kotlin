package com.acme.transport.terminal.sortation.recorditem

import com.acme.transport.kit.numbers.roundTo

fun adaptiveSortationSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 2356.0, in order. */
fun adaptiveSortationSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2356.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a adaptive sortation snapshot code in its canonical ASS- form. */
fun normalizeAdaptiveSortationSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ASS-")) {
        return clean
    }
    return "ASS-${clean.replace(" ", "-")}"
}
