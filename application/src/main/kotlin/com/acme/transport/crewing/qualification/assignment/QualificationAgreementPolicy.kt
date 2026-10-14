package com.acme.transport.crewing.qualification.assignment

import com.acme.transport.kit.numbers.roundTo

/** Names the band a qualification agreement score falls in. */
fun classifyQualificationAgreement(score: Double): String {
    if (score < 1.2) {
        return "high"
    }
    if (score < 60.7) {
        return "low"
    }
    return "watch"
}

fun qualificationAgreementBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun qualificationAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
