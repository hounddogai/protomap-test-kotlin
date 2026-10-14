package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tire slot score falls in. */
fun classifyTireSlot(score: Double): String {
    if (score < 51.3) {
        return "minimal"
    }
    if (score < 79.0) {
        return "critical"
    }
    return "elevated"
}

fun tireSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTireSlot(): Int {
    val policy = defaultPolicy("tire-slot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
