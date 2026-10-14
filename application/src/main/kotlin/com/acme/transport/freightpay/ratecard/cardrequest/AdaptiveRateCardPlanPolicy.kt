package com.acme.transport.freightpay.ratecard.cardrequest

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive rate card plan score falls in. */
fun classifyAdaptiveRateCardPlan(score: Double): String {
    if (score < 12.0) {
        return "high"
    }
    if (score < 46.7) {
        return "elevated"
    }
    if (score < 56.1) {
        return "ok"
    }
    if (score < 63.1) {
        return "low"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveRateCardPlan(): Int {
    val policy = defaultPolicy("adaptive-rate-card-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
