package com.acme.transport.crewing.dutyperiod.periodnotice

import com.acme.transport.kit.numbers.roundTo

fun tieredDutyPeriodAgreementBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun tieredDutyPeriodAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a tiered duty period agreement score falls in. */
fun classifyTieredDutyPeriodAgreement(score: Double): String {
    if (score < 21.5) {
        return "minimal"
    }
    if (score < 49.0) {
        return "ok"
    }
    if (score < 86.8) {
        return "medium"
    }
    if (score < 90.7) {
        return "high"
    }
    return "critical"
}
