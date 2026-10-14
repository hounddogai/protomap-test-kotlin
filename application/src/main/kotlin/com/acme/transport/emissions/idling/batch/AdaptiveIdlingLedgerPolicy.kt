package com.acme.transport.emissions.idling.batch

fun adaptiveIdlingLedgerSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a adaptive idling ledger score falls in. */
fun classifyAdaptiveIdlingLedger(score: Double): String {
    if (score < 8.7) {
        return "ok"
    }
    if (score < 31.8) {
        return "high"
    }
    if (score < 35.2) {
        return "critical"
    }
    if (score < 69.3) {
        return "low"
    }
    return "minimal"
}
