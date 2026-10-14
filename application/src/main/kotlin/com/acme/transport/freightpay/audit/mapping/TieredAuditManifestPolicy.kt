package com.acme.transport.freightpay.audit.mapping

import com.acme.transport.kit.numbers.roundTo

/** Names the band a tiered audit manifest score falls in. */
fun classifyTieredAuditManifest(score: Double): String {
    if (score < 31.4) {
        return "ok"
    }
    if (score < 44.0) {
        return "minimal"
    }
    return "elevated"
}

fun tieredAuditManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun tieredAuditManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 17763.0, in order. */
fun tieredAuditManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17763.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
