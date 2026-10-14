package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun pendingLanePlanCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.69
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingLanePlanCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a pending lane plan checkpoint score falls in. */
fun classifyPendingLanePlanCheckpoint(score: Double): String {
    if (score < 47.3) {
        return "high"
    }
    if (score < 57.6) {
        return "critical"
    }
    if (score < 64.6) {
        return "ok"
    }
    if (score < 66.3) {
        return "low"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingLanePlanCheckpoint(): Int {
    val policy = defaultPolicy("pending-lane-plan-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
