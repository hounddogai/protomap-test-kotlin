package com.acme.transport.linehaul.capacity.capacityallocation

import com.acme.transport.kit.numbers.roundTo

/** Puts a legacy capacity snapshot code in its canonical LCS- form. */
fun normalizeLegacyCapacitySnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCS-")) {
        return clean
    }
    return "LCS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 55955.0, in order. */
fun legacyCapacitySnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 55955.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun legacyCapacitySnapshotBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun legacyCapacitySnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
