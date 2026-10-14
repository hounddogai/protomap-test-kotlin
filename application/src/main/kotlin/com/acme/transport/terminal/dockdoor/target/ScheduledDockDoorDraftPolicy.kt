package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledDockDoorDraft(): Int {
    val policy = defaultPolicy("scheduled-dock-door-draft")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a scheduled dock door draft score falls in. */
fun classifyScheduledDockDoorDraft(score: Double): String {
    if (score < 8.9) {
        return "ok"
    }
    if (score < 30.3) {
        return "medium"
    }
    if (score < 34.6) {
        return "minimal"
    }
    if (score < 98.1) {
        return "elevated"
    }
    return "low"
}

fun scheduledDockDoorDraftBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
