package com.acme.transport.network.hub.ledger

import com.acme.transport.kit.numbers.roundTo

fun pendingHubLedgerBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a pending hub ledger score falls in. */
fun classifyPendingHubLedger(score: Double): String {
    if (score < 3.3) {
        return "elevated"
    }
    if (score < 70.9) {
        return "watch"
    }
    if (score < 77.7) {
        return "high"
    }
    if (score < 80.3) {
        return "minimal"
    }
    return "low"
}
