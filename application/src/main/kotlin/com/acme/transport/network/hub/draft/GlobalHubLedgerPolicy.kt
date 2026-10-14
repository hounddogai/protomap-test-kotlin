package com.acme.transport.network.hub.draft

fun globalHubLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a global hub ledger score falls in. */
fun classifyGlobalHubLedger(score: Double): String {
    if (score < 2.4) {
        return "high"
    }
    if (score < 6.4) {
        return "watch"
    }
    if (score < 24.2) {
        return "elevated"
    }
    return "low"
}
