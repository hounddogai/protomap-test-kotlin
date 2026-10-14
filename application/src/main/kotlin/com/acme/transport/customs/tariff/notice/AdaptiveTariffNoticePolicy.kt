package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.numbers.roundTo

/** Names the band a adaptive tariff notice score falls in. */
fun classifyAdaptiveTariffNotice(score: Double): String {
    if (score < 31.6) {
        return "ok"
    }
    if (score < 35.8) {
        return "critical"
    }
    if (score < 45.2) {
        return "elevated"
    }
    return "high"
}

fun adaptiveTariffNoticeBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun adaptiveTariffNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
