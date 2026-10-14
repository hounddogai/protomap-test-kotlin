package com.acme.transport.crewing.roster.recorditem

import com.acme.transport.kit.numbers.roundTo

fun lenientRosterLedgerBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a lenient roster ledger score falls in. */
fun classifyLenientRosterLedger(score: Double): String {
    if (score < 22.0) {
        return "ok"
    }
    if (score < 93.4) {
        return "minimal"
    }
    return "elevated"
}
