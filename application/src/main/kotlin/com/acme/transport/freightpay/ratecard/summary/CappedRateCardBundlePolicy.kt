package com.acme.transport.freightpay.ratecard.summary

import com.acme.transport.kit.retry.defaultPolicy

fun cappedRateCardBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedRateCardBundle(): Int {
    val policy = defaultPolicy("capped-rate-card-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a capped rate card bundle score falls in. */
fun classifyCappedRateCardBundle(score: Double): String {
    if (score < 24.9) {
        return "low"
    }
    if (score < 59.5) {
        return "minimal"
    }
    if (score < 82.2) {
        return "critical"
    }
    if (score < 89.9) {
        return "high"
    }
    return "watch"
}
