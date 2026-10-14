package com.acme.transport.network.transittime.baseline

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a tiered transit time window code in its canonical TTTW- form. */
fun normalizeTieredTransitTimeWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTTW-")) {
        return clean
    }
    return "TTTW-${clean.replace(" ", "-")}"
}

fun tieredTransitTimeWindowBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a tiered transit time window score falls in. */
fun classifyTieredTransitTimeWindow(score: Double): String {
    if (score < 1.0) {
        return "medium"
    }
    if (score < 34.3) {
        return "low"
    }
    if (score < 49.9) {
        return "minimal"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredTransitTimeWindow(): Int {
    val policy = defaultPolicy("tiered-transit-time-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
