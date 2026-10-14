package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import com.acme.transport.kit.numbers.roundTo

fun weightedDisputeSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun weightedDisputeSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a weighted dispute snapshot code in its canonical WDS- form. */
fun normalizeWeightedDisputeSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDS-")) {
        return clean
    }
    return "WDS-${clean.replace(" ", "-")}"
}
