package com.acme.transport.terminal.dockdoor.profile

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a strict dock door batch score falls in. */
fun classifyStrictDockDoorBatch(score: Double): String {
    if (score < 14.7) {
        return "watch"
    }
    if (score < 24.7) {
        return "ok"
    }
    if (score < 60.7) {
        return "minimal"
    }
    return "elevated"
}

fun strictDockDoorBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a strict dock door batch code in its canonical SDDB- form. */
fun normalizeStrictDockDoorBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDDB-")) {
        return clean
    }
    return "SDDB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictDockDoorBatch(): Int {
    val policy = defaultPolicy("strict-dock-door-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
