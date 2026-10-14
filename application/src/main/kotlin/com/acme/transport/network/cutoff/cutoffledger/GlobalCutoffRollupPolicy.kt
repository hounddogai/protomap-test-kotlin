package com.acme.transport.network.cutoff.cutoffledger

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a global cutoff rollup score falls in. */
fun classifyGlobalCutoffRollup(score: Double): String {
    if (score < 12.3) {
        return "medium"
    }
    if (score < 26.8) {
        return "watch"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalCutoffRollup(): Int {
    val policy = defaultPolicy("global-cutoff-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalCutoffRollupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.86
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun globalCutoffRollupBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
