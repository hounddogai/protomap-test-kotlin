package com.acme.transport.network.cutoff.cutoffentry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a regional cutoff claim code in its canonical RCC- form. */
fun normalizeRegionalCutoffClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCC-")) {
        return clean
    }
    return "RCC-${clean.replace(" ", "-")}"
}

/** Names the band a regional cutoff claim score falls in. */
fun classifyRegionalCutoffClaim(score: Double): String {
    if (score < 26.2) {
        return "ok"
    }
    if (score < 42.4) {
        return "watch"
    }
    if (score < 67.4) {
        return "high"
    }
    return "critical"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalCutoffClaim(): Int {
    val policy = defaultPolicy("regional-cutoff-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalCutoffClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
