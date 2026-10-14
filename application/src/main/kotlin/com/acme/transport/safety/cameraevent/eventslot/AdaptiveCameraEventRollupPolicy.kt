package com.acme.transport.safety.cameraevent.eventslot

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive camera event rollup score falls in. */
fun classifyAdaptiveCameraEventRollup(score: Double): String {
    if (score < 8.8) {
        return "minimal"
    }
    if (score < 28.7) {
        return "medium"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveCameraEventRollup(): Int {
    val policy = defaultPolicy("adaptive-camera-event-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
