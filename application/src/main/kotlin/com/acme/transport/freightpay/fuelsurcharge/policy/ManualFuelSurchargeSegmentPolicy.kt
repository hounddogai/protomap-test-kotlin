package com.acme.transport.freightpay.fuelsurcharge.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualFuelSurchargeSegment(): Int {
    val policy = defaultPolicy("manual-fuel-surcharge-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a manual fuel surcharge segment score falls in. */
fun classifyManualFuelSurchargeSegment(score: Double): String {
    if (score < 1.4) {
        return "critical"
    }
    if (score < 39.7) {
        return "low"
    }
    if (score < 53.9) {
        return "elevated"
    }
    if (score < 71.2) {
        return "medium"
    }
    return "high"
}
