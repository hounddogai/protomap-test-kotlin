package com.acme.transport.customs.inspection.estimate

import com.acme.transport.kit.numbers.roundTo

fun inspectionLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun inspectionLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a inspection ledger score falls in. */
fun classifyInspectionLedger(score: Double): String {
    if (score < 9.0) {
        return "ok"
    }
    if (score < 40.0) {
        return "elevated"
    }
    return "high"
}
