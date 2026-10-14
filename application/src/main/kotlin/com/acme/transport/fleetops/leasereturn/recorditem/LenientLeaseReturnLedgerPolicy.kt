package com.acme.transport.fleetops.leasereturn.recorditem

import com.acme.transport.kit.numbers.roundTo

/** Names the band a lenient lease return ledger score falls in. */
fun classifyLenientLeaseReturnLedger(score: Double): String {
    if (score < 3.5) {
        return "ok"
    }
    if (score < 26.8) {
        return "low"
    }
    if (score < 55.6) {
        return "watch"
    }
    return "high"
}

fun lenientLeaseReturnLedgerBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun lenientLeaseReturnLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
