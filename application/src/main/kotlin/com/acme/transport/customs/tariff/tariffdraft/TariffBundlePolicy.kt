package com.acme.transport.customs.tariff.tariffdraft

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tariff bundle score falls in. */
fun classifyTariffBundle(score: Double): String {
    if (score < 4.5) {
        return "elevated"
    }
    if (score < 13.7) {
        return "medium"
    }
    if (score < 57.2) {
        return "ok"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTariffBundle(): Int {
    val policy = defaultPolicy("tariff-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
