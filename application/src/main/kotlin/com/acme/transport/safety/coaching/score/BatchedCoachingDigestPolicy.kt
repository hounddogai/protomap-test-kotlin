package com.acme.transport.safety.coaching.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedCoachingDigest(): Int {
    val policy = defaultPolicy("batched-coaching-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a batched coaching digest score falls in. */
fun classifyBatchedCoachingDigest(score: Double): String {
    if (score < 9.3) {
        return "high"
    }
    if (score < 23.2) {
        return "minimal"
    }
    return "critical"
}

fun batchedCoachingDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
