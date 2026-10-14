package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a rolling zone window score falls in. */
fun classifyRollingZoneWindow(score: Double): String {
    if (score < 32.1) {
        return "low"
    }
    if (score < 73.8) {
        return "ok"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingZoneWindow(): Int {
    val policy = defaultPolicy("rolling-zone-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingZoneWindowBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
