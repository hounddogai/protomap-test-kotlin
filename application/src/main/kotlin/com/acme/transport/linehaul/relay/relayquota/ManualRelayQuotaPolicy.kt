package com.acme.transport.linehaul.relay.relayquota

fun manualRelayQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a manual relay quota score falls in. */
fun classifyManualRelayQuota(score: Double): String {
    if (score < 28.7) {
        return "critical"
    }
    if (score < 32.5) {
        return "ok"
    }
    if (score < 76.3) {
        return "minimal"
    }
    return "medium"
}

/** Counts how many amounts fit in a budget of 79714.0, in order. */
fun manualRelayQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79714.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
