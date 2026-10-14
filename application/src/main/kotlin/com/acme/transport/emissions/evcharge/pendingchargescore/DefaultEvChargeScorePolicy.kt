package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultEvChargeScore(): Int {
    val policy = defaultPolicy("default-ev-charge-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultEvChargeScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun defaultEvChargeScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.26
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default ev charge score score falls in. */
fun classifyDefaultEvChargeScore(score: Double): String {
    if (score < 36.6) {
        return "medium"
    }
    if (score < 51.4) {
        return "low"
    }
    if (score < 95.9) {
        return "minimal"
    }
    return "ok"
}
