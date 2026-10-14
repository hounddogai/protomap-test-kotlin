package com.acme.transport.fleetops.tire.draft

import com.acme.transport.kit.numbers.roundTo

fun weightedTireLedgerBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a weighted tire ledger score falls in. */
fun classifyWeightedTireLedger(score: Double): String {
    if (score < 15.8) {
        return "critical"
    }
    if (score < 22.6) {
        return "minimal"
    }
    if (score < 35.7) {
        return "medium"
    }
    if (score < 49.6) {
        return "ok"
    }
    return "high"
}
