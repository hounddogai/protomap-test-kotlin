package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun disputeMetricBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a dispute metric score falls in. */
fun classifyDisputeMetric(score: Double): String {
    if (score < 20.2) {
        return "medium"
    }
    if (score < 49.8) {
        return "low"
    }
    if (score < 62.1) {
        return "ok"
    }
    return "high"
}

fun disputeMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.21
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDisputeMetric(): Int {
    val policy = defaultPolicy("dispute-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
