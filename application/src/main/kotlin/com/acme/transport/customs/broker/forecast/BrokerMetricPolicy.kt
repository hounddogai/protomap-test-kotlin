package com.acme.transport.customs.broker.forecast

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a broker metric code in its canonical BM- form. */
fun normalizeBrokerMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BM-")) {
        return clean
    }
    return "BM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBrokerMetric(): Int {
    val policy = defaultPolicy("broker-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
