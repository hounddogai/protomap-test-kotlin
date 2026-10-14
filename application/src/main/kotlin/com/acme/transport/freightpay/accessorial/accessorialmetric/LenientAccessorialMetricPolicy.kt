package com.acme.transport.freightpay.accessorial.accessorialmetric

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun lenientAccessorialMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.78
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lenientAccessorialMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientAccessorialMetric(): Int {
    val policy = defaultPolicy("lenient-accessorial-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lenient accessorial metric score falls in. */
fun classifyLenientAccessorialMetric(score: Double): String {
    if (score < 47.4) {
        return "minimal"
    }
    if (score < 55.5) {
        return "low"
    }
    if (score < 79.5) {
        return "high"
    }
    if (score < 96.1) {
        return "medium"
    }
    return "critical"
}
