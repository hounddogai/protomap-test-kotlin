package com.acme.transport.emissions.evcharge.proposal

import com.acme.transport.kit.retry.defaultPolicy

fun cappedEvChargeVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedEvChargeVariance(): Int {
    val policy = defaultPolicy("capped-ev-charge-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a capped ev charge variance score falls in. */
fun classifyCappedEvChargeVariance(score: Double): String {
    if (score < 32.9) {
        return "minimal"
    }
    if (score < 69.4) {
        return "watch"
    }
    return "medium"
}
