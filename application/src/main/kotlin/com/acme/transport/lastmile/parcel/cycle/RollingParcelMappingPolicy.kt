package com.acme.transport.lastmile.parcel.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingParcelMapping(): Int {
    val policy = defaultPolicy("rolling-parcel-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling parcel mapping score falls in. */
fun classifyRollingParcelMapping(score: Double): String {
    if (score < 14.0) {
        return "elevated"
    }
    if (score < 27.4) {
        return "minimal"
    }
    if (score < 79.3) {
        return "medium"
    }
    return "ok"
}
