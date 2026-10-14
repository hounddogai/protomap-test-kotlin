package com.acme.transport.lastmile.attempt.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeAttemptBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAttemptBaseline(): Int {
    val policy = defaultPolicy("attempt-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a attempt baseline score falls in. */
fun classifyAttemptBaseline(score: Double): String {
    if (score < 7.0) {
        return "elevated"
    }
    if (score < 14.5) {
        return "critical"
    }
    if (score < 22.8) {
        return "ok"
    }
    if (score < 86.1) {
        return "low"
    }
    return "medium"
}

fun attemptBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.91
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
