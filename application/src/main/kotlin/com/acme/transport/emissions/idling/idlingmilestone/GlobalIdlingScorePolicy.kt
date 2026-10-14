package com.acme.transport.emissions.idling.idlingmilestone

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a global idling score score falls in. */
fun classifyGlobalIdlingScore(score: Double): String {
    if (score < 21.2) {
        return "critical"
    }
    if (score < 35.1) {
        return "minimal"
    }
    if (score < 91.1) {
        return "watch"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalIdlingScore(): Int {
    val policy = defaultPolicy("global-idling-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalIdlingScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
