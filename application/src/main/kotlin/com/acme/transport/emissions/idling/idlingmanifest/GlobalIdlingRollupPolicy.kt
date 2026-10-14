package com.acme.transport.emissions.idling.idlingmanifest

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun globalIdlingRollupBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a global idling rollup score falls in. */
fun classifyGlobalIdlingRollup(score: Double): String {
    if (score < 45.7) {
        return "medium"
    }
    if (score < 88.4) {
        return "high"
    }
    return "watch"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalIdlingRollup(): Int {
    val policy = defaultPolicy("global-idling-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a global idling rollup code in its canonical GIR- form. */
fun normalizeGlobalIdlingRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GIR-")) {
        return clean
    }
    return "GIR-${clean.replace(" ", "-")}"
}
