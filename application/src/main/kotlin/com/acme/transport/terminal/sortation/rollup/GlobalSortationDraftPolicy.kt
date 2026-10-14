package com.acme.transport.terminal.sortation.rollup

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun globalSortationDraftBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a global sortation draft score falls in. */
fun classifyGlobalSortationDraft(score: Double): String {
    if (score < 10.8) {
        return "watch"
    }
    if (score < 62.8) {
        return "high"
    }
    if (score < 68.1) {
        return "critical"
    }
    if (score < 75.9) {
        return "medium"
    }
    return "elevated"
}

fun globalSortationDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalSortationDraft(): Int {
    val policy = defaultPolicy("global-sortation-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
