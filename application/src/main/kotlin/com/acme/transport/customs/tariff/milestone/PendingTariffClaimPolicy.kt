package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a pending tariff claim score falls in. */
fun classifyPendingTariffClaim(score: Double): String {
    if (score < 18.9) {
        return "minimal"
    }
    if (score < 32.4) {
        return "watch"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingTariffClaim(): Int {
    val policy = defaultPolicy("pending-tariff-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
