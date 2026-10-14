package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a dispute profile score falls in. */
fun classifyDisputeProfile(score: Double): String {
    if (score < 37.4) {
        return "low"
    }
    if (score < 47.3) {
        return "watch"
    }
    if (score < 84.8) {
        return "elevated"
    }
    return "minimal"
}

/** Puts a dispute profile code in its canonical DP- form. */
fun normalizeDisputeProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDisputeProfile(): Int {
    val policy = defaultPolicy("dispute-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun disputeProfileBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
