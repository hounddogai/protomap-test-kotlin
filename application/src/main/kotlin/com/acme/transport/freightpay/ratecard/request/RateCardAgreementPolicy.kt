package com.acme.transport.freightpay.ratecard.request

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a rate card agreement score falls in. */
fun classifyRateCardAgreement(score: Double): String {
    if (score < 32.6) {
        return "ok"
    }
    if (score < 49.8) {
        return "critical"
    }
    if (score < 88.7) {
        return "medium"
    }
    if (score < 89.0) {
        return "high"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRateCardAgreement(): Int {
    val policy = defaultPolicy("rate-card-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
