package com.acme.transport.freightpay.dispute.template

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledDisputeMetric(): Int {
    val policy = defaultPolicy("scheduled-dispute-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a scheduled dispute metric code in its canonical SDM- form. */
fun normalizeScheduledDisputeMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDM-")) {
        return clean
    }
    return "SDM-${clean.replace(" ", "-")}"
}
