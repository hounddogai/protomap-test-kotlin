package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun weightedTrailerYardSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a weighted trailer yard snapshot score falls in. */
fun classifyWeightedTrailerYardSnapshot(score: Double): String {
    if (score < 9.4) {
        return "ok"
    }
    if (score < 33.5) {
        return "minimal"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedTrailerYardSnapshot(): Int {
    val policy = defaultPolicy("weighted-trailer-yard-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedTrailerYardSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
