package com.acme.transport.network.servicearea.cycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingServiceAreaCycle(): Int {
    val policy = defaultPolicy("rolling-service-area-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingServiceAreaCycleBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a rolling service area cycle score falls in. */
fun classifyRollingServiceAreaCycle(score: Double): String {
    if (score < 65.8) {
        return "low"
    }
    if (score < 67.9) {
        return "high"
    }
    if (score < 68.1) {
        return "watch"
    }
    if (score < 90.8) {
        return "medium"
    }
    return "critical"
}

fun rollingServiceAreaCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
