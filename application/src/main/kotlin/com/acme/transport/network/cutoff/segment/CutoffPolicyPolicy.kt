package com.acme.transport.network.cutoff.segment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a cutoff policy score falls in. */
fun classifyCutoffPolicy(score: Double): String {
    if (score < 44.4) {
        return "ok"
    }
    if (score < 79.4) {
        return "minimal"
    }
    return "high"
}

fun cutoffPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCutoffPolicy(): Int {
    val policy = defaultPolicy("cutoff-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun cutoffPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
