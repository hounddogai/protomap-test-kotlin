package com.acme.transport.terminal.dockdoor.target

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a dock door target score falls in. */
fun classifyDockDoorTarget(score: Double): String {
    if (score < 14.0) {
        return "critical"
    }
    if (score < 20.5) {
        return "medium"
    }
    if (score < 75.6) {
        return "low"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorTarget(): Int {
    val policy = defaultPolicy("dock-door-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
