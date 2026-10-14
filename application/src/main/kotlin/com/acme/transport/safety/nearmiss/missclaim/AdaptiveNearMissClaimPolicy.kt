package com.acme.transport.safety.nearmiss.missclaim

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveNearMissClaim(): Int {
    val policy = defaultPolicy("adaptive-near-miss-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive near miss claim code in its canonical ANMC- form. */
fun normalizeAdaptiveNearMissClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ANMC-")) {
        return clean
    }
    return "ANMC-${clean.replace(" ", "-")}"
}

fun adaptiveNearMissClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
