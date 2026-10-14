package com.acme.transport.linehaul.tender.review

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun defaultTenderDigestBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a default tender digest score falls in. */
fun classifyDefaultTenderDigest(score: Double): String {
    if (score < 31.6) {
        return "low"
    }
    if (score < 38.1) {
        return "high"
    }
    if (score < 50.8) {
        return "watch"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultTenderDigest(): Int {
    val policy = defaultPolicy("default-tender-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
