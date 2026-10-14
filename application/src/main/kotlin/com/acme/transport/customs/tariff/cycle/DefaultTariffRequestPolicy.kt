package com.acme.transport.customs.tariff.cycle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultTariffRequest(): Int {
    val policy = defaultPolicy("default-tariff-request")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultTariffRequestBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a default tariff request score falls in. */
fun classifyDefaultTariffRequest(score: Double): String {
    if (score < 43.4) {
        return "watch"
    }
    if (score < 71.0) {
        return "ok"
    }
    if (score < 83.7) {
        return "minimal"
    }
    return "low"
}
