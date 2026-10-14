package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun weightedLanePlanWindowBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun weightedLanePlanWindowSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a weighted lane plan window score falls in. */
fun classifyWeightedLanePlanWindow(score: Double): String {
    if (score < 18.2) {
        return "medium"
    }
    if (score < 22.1) {
        return "watch"
    }
    if (score < 74.0) {
        return "elevated"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedLanePlanWindow(): Int {
    val policy = defaultPolicy("weighted-lane-plan-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
