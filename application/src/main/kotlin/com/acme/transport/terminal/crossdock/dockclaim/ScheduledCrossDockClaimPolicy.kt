package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScheduledCrossDockClaim(): Int {
    val policy = defaultPolicy("scheduled-cross-dock-claim")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scheduledCrossDockClaimBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a scheduled cross dock claim score falls in. */
fun classifyScheduledCrossDockClaim(score: Double): String {
    if (score < 31.0) {
        return "critical"
    }
    if (score < 34.8) {
        return "watch"
    }
    if (score < 76.3) {
        return "elevated"
    }
    return "ok"
}

/** Puts a scheduled cross dock claim code in its canonical SCDC- form. */
fun normalizeScheduledCrossDockClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCDC-")) {
        return clean
    }
    return "SCDC-${clean.replace(" ", "-")}"
}
