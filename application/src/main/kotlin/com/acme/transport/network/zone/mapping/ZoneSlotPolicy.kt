package com.acme.transport.network.zone.mapping

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a zone slot score falls in. */
fun classifyZoneSlot(score: Double): String {
    if (score < 53.4) {
        return "high"
    }
    if (score < 54.8) {
        return "low"
    }
    if (score < 63.5) {
        return "watch"
    }
    return "elevated"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneSlot(): Int {
    val policy = defaultPolicy("zone-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
