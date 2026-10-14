package com.acme.transport.freightpay.freightinvoice.exception

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive freight invoice agreement score falls in. */
fun classifyAdaptiveFreightInvoiceAgreement(score: Double): String {
    if (score < 13.2) {
        return "ok"
    }
    if (score < 23.9) {
        return "medium"
    }
    if (score < 36.2) {
        return "low"
    }
    if (score < 41.6) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveFreightInvoiceAgreement(): Int {
    val policy = defaultPolicy("adaptive-freight-invoice-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
