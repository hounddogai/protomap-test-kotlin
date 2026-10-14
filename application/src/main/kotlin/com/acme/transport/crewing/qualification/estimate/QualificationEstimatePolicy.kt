package com.acme.transport.crewing.qualification.estimate

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a qualification estimate score falls in. */
fun classifyQualificationEstimate(score: Double): String {
    if (score < 20.5) {
        return "ok"
    }
    if (score < 47.8) {
        return "high"
    }
    if (score < 62.7) {
        return "low"
    }
    if (score < 77.4) {
        return "medium"
    }
    return "elevated"
}

fun qualificationEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.13
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun qualificationEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationEstimate(): Int {
    val policy = defaultPolicy("qualification-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
