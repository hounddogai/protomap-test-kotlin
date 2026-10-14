package com.acme.transport.network.transittime.batch

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun transitTimeBatchBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTransitTimeBatch(): Int {
    val policy = defaultPolicy("transit-time-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a transit time batch code in its canonical TTB- form. */
fun normalizeTransitTimeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTB-")) {
        return clean
    }
    return "TTB-${clean.replace(" ", "-")}"
}

/** Names the band a transit time batch score falls in. */
fun classifyTransitTimeBatch(score: Double): String {
    if (score < 24.2) {
        return "critical"
    }
    if (score < 30.4) {
        return "elevated"
    }
    return "minimal"
}
