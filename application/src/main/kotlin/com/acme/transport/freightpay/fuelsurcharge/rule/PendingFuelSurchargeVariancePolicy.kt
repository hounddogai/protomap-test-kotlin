package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingFuelSurchargeVariance(): Int {
    val policy = defaultPolicy("pending-fuel-surcharge-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending fuel surcharge variance score falls in. */
fun classifyPendingFuelSurchargeVariance(score: Double): String {
    if (score < 34.5) {
        return "medium"
    }
    if (score < 41.0) {
        return "watch"
    }
    if (score < 49.9) {
        return "ok"
    }
    return "minimal"
}
