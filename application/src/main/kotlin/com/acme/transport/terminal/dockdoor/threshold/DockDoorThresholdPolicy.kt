package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorThreshold(): Int {
    val policy = defaultPolicy("dock-door-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a dock door threshold score falls in. */
fun classifyDockDoorThreshold(score: Double): String {
    if (score < 19.7) {
        return "ok"
    }
    if (score < 43.2) {
        return "medium"
    }
    if (score < 45.4) {
        return "watch"
    }
    if (score < 72.1) {
        return "elevated"
    }
    return "critical"
}

fun dockDoorThresholdBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
