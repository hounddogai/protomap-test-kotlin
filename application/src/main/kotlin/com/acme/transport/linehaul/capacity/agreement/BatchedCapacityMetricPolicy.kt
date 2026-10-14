package com.acme.transport.linehaul.capacity.agreement

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedCapacityMetric(): Int {
    val policy = defaultPolicy("batched-capacity-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun batchedCapacityMetricBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a batched capacity metric code in its canonical BCM- form. */
fun normalizeBatchedCapacityMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BCM-")) {
        return clean
    }
    return "BCM-${clean.replace(" ", "-")}"
}
