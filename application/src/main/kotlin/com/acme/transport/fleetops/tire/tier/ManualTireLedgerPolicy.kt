package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualTireLedger(): Int {
    val policy = defaultPolicy("manual-tire-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual tire ledger score falls in. */
fun classifyManualTireLedger(score: Double): String {
    if (score < 4.9) {
        return "watch"
    }
    if (score < 46.0) {
        return "elevated"
    }
    if (score < 66.8) {
        return "ok"
    }
    if (score < 85.7) {
        return "high"
    }
    return "minimal"
}
