package com.acme.transport.network.zone.group

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a zone profile score falls in. */
fun classifyZoneProfile(score: Double): String {
    if (score < 4.4) {
        return "critical"
    }
    if (score < 41.1) {
        return "watch"
    }
    if (score < 63.5) {
        return "low"
    }
    if (score < 70.6) {
        return "elevated"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneProfile(): Int {
    val policy = defaultPolicy("zone-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 31532.0, in order. */
fun zoneProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31532.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun zoneProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}
