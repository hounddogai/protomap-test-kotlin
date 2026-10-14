package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a capped dock door snapshot score falls in. */
fun classifyCappedDockDoorSnapshot(score: Double): String {
    if (score < 11.0) {
        return "high"
    }
    if (score < 26.0) {
        return "watch"
    }
    if (score < 57.7) {
        return "critical"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedDockDoorSnapshot(): Int {
    val policy = defaultPolicy("capped-dock-door-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
