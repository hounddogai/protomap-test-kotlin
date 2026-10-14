package com.acme.transport.lastmile.attempt.attemptprofile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedAttemptProfile(): Int {
    val policy = defaultPolicy("weighted-attempt-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted attempt profile score falls in. */
fun classifyWeightedAttemptProfile(score: Double): String {
    if (score < 3.6) {
        return "critical"
    }
    if (score < 79.9) {
        return "medium"
    }
    return "ok"
}

fun weightedAttemptProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
