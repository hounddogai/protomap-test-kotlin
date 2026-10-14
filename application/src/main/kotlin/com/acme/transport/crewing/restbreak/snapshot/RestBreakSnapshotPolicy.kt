package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRestBreakSnapshot(): Int {
    val policy = defaultPolicy("rest-break-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rest break snapshot score falls in. */
fun classifyRestBreakSnapshot(score: Double): String {
    if (score < 6.8) {
        return "medium"
    }
    if (score < 92.3) {
        return "elevated"
    }
    if (score < 98.2) {
        return "watch"
    }
    return "critical"
}

fun restBreakSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a rest break snapshot code in its canonical RBS- form. */
fun normalizeRestBreakSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBS-")) {
        return clean
    }
    return "RBS-${clean.replace(" ", "-")}"
}
