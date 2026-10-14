package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun defaultSwapCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default swap checkpoint score falls in. */
fun classifyDefaultSwapCheckpoint(score: Double): String {
    if (score < 1.6) {
        return "elevated"
    }
    if (score < 24.4) {
        return "medium"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultSwapCheckpoint(): Int {
    val policy = defaultPolicy("default-swap-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultSwapCheckpointBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
