package com.acme.transport.emissions.evcharge.overrideitem

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a manual ev charge override score falls in. */
fun classifyManualEvChargeOverride(score: Double): String {
    if (score < 5.3) {
        return "elevated"
    }
    if (score < 69.4) {
        return "watch"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualEvChargeOverride(): Int {
    val policy = defaultPolicy("manual-ev-charge-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
