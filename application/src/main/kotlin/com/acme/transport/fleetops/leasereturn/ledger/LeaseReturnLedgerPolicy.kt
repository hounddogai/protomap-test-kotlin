package com.acme.transport.fleetops.leasereturn.ledger

import com.acme.transport.kit.numbers.roundTo

fun leaseReturnLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun leaseReturnLedgerBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
