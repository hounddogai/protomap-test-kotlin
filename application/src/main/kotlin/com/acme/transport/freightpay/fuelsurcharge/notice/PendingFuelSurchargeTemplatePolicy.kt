package com.acme.transport.freightpay.fuelsurcharge.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingFuelSurchargeTemplate(): Int {
    val policy = defaultPolicy("pending-fuel-surcharge-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending fuel surcharge template score falls in. */
fun classifyPendingFuelSurchargeTemplate(score: Double): String {
    if (score < 16.2) {
        return "elevated"
    }
    if (score < 52.5) {
        return "minimal"
    }
    return "critical"
}
