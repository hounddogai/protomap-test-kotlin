package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched lease return forecast code in its canonical BLRF- form. */
fun normalizeBatchedLeaseReturnForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BLRF-")) {
        return clean
    }
    return "BLRF-${clean.replace(" ", "-")}"
}

/** Names the band a batched lease return forecast score falls in. */
fun classifyBatchedLeaseReturnForecast(score: Double): String {
    if (score < 2.7) {
        return "critical"
    }
    if (score < 86.4) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedLeaseReturnForecast(): Int {
    val policy = defaultPolicy("batched-lease-return-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
