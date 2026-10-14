package com.acme.transport.fleetops.utilization.ledger

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun batchedUtilizationMetricBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a batched utilization metric score falls in. */
fun classifyBatchedUtilizationMetric(score: Double): String {
    if (score < 11.5) {
        return "low"
    }
    if (score < 14.2) {
        return "medium"
    }
    if (score < 67.0) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedUtilizationMetric(): Int {
    val policy = defaultPolicy("batched-utilization-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
