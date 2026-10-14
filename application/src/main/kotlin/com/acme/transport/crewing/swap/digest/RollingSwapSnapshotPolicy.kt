package com.acme.transport.crewing.swap.digest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling swap snapshot code in its canonical RSS- form. */
fun normalizeRollingSwapSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RSS-")) {
        return clean
    }
    return "RSS-${clean.replace(" ", "-")}"
}

fun rollingSwapSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingSwapSnapshot(): Int {
    val policy = defaultPolicy("rolling-swap-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a rolling swap snapshot score falls in. */
fun classifyRollingSwapSnapshot(score: Double): String {
    if (score < 20.3) {
        return "medium"
    }
    if (score < 34.7) {
        return "minimal"
    }
    if (score < 37.7) {
        return "critical"
    }
    if (score < 93.1) {
        return "high"
    }
    return "ok"
}
