package com.acme.transport.network.cutoff.claim

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun cutoffScheduleBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCutoffSchedule(): Int {
    val policy = defaultPolicy("cutoff-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a cutoff schedule score falls in. */
fun classifyCutoffSchedule(score: Double): String {
    if (score < 26.5) {
        return "watch"
    }
    if (score < 29.6) {
        return "low"
    }
    if (score < 84.7) {
        return "high"
    }
    return "critical"
}
