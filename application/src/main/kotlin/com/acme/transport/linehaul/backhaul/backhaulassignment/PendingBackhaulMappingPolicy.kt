package com.acme.transport.linehaul.backhaul.backhaulassignment

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a pending backhaul mapping score falls in. */
fun classifyPendingBackhaulMapping(score: Double): String {
    if (score < 5.8) {
        return "medium"
    }
    if (score < 27.6) {
        return "watch"
    }
    if (score < 47.8) {
        return "elevated"
    }
    if (score < 60.9) {
        return "minimal"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingBackhaulMapping(): Int {
    val policy = defaultPolicy("pending-backhaul-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
