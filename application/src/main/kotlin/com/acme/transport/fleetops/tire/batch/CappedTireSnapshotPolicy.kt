package com.acme.transport.fleetops.tire.batch

import com.acme.transport.kit.retry.defaultPolicy

fun cappedTireSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a capped tire snapshot score falls in. */
fun classifyCappedTireSnapshot(score: Double): String {
    if (score < 30.8) {
        return "elevated"
    }
    if (score < 56.7) {
        return "watch"
    }
    if (score < 62.4) {
        return "medium"
    }
    if (score < 65.5) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedTireSnapshot(): Int {
    val policy = defaultPolicy("capped-tire-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
