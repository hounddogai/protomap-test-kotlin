package com.acme.transport.fleetops.utilization.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a utilization metric code in its canonical UM- form. */
fun normalizeUtilizationMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("UM-")) {
        return clean
    }
    return "UM-${clean.replace(" ", "-")}"
}

fun utilizationMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun utilizationMetricBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationMetric(): Int {
    val policy = defaultPolicy("utilization-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
