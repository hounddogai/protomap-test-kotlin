package com.acme.transport.freightpay.accessorial.score

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalAccessorialScore(): Int {
    val policy = defaultPolicy("global-accessorial-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalAccessorialScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun globalAccessorialScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.75
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a global accessorial score score falls in. */
fun classifyGlobalAccessorialScore(score: Double): String {
    if (score < 81.5) {
        return "minimal"
    }
    if (score < 87.8) {
        return "high"
    }
    return "medium"
}
