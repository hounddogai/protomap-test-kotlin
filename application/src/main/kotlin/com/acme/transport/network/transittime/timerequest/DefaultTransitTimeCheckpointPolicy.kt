package com.acme.transport.network.transittime.timerequest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun defaultTransitTimeCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default transit time checkpoint score falls in. */
fun classifyDefaultTransitTimeCheckpoint(score: Double): String {
    if (score < 43.6) {
        return "critical"
    }
    if (score < 50.1) {
        return "medium"
    }
    if (score < 81.0) {
        return "low"
    }
    return "ok"
}

fun defaultTransitTimeCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultTransitTimeCheckpoint(): Int {
    val policy = defaultPolicy("default-transit-time-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
