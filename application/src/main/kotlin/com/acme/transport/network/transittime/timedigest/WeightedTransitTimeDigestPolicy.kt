package com.acme.transport.network.transittime.timedigest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedTransitTimeDigest(): Int {
    val policy = defaultPolicy("weighted-transit-time-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedTransitTimeDigestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a weighted transit time digest code in its canonical WTTD- form. */
fun normalizeWeightedTransitTimeDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WTTD-")) {
        return clean
    }
    return "WTTD-${clean.replace(" ", "-")}"
}

fun weightedTransitTimeDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
