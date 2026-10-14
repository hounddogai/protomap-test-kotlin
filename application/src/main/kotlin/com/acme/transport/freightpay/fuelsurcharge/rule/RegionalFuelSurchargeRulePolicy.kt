package com.acme.transport.freightpay.fuelsurcharge.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a regional fuel surcharge rule score falls in. */
fun classifyRegionalFuelSurchargeRule(score: Double): String {
    if (score < 9.0) {
        return "minimal"
    }
    if (score < 30.0) {
        return "medium"
    }
    if (score < 44.1) {
        return "critical"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalFuelSurchargeRule(): Int {
    val policy = defaultPolicy("regional-fuel-surcharge-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
