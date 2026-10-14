package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.numbers.roundTo

fun pendingTariffAgreementBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a pending tariff agreement score falls in. */
fun classifyPendingTariffAgreement(score: Double): String {
    if (score < 15.3) {
        return "minimal"
    }
    if (score < 31.7) {
        return "watch"
    }
    if (score < 42.2) {
        return "ok"
    }
    if (score < 68.6) {
        return "low"
    }
    return "critical"
}

fun pendingTariffAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
