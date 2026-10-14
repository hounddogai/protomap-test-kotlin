package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaSummary(): Int {
    val policy = defaultPolicy("service-area-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun serviceAreaSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
