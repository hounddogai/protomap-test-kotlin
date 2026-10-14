package com.acme.transport.lastmile.locker.claim

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a pending locker claim code in its canonical PLC- form. */
fun normalizePendingLockerClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PLC-")) {
        return clean
    }
    return "PLC-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingLockerClaim(): Int {
    val policy = defaultPolicy("pending-locker-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending locker claim score falls in. */
fun classifyPendingLockerClaim(score: Double): String {
    if (score < 46.9) {
        return "critical"
    }
    if (score < 52.4) {
        return "high"
    }
    if (score < 67.5) {
        return "elevated"
    }
    return "low"
}

fun pendingLockerClaimBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
