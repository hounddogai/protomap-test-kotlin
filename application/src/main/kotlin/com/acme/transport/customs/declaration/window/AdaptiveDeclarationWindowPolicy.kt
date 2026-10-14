package com.acme.transport.customs.declaration.window

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a adaptive declaration window score falls in. */
fun classifyAdaptiveDeclarationWindow(score: Double): String {
    if (score < 31.1) {
        return "high"
    }
    if (score < 45.4) {
        return "critical"
    }
    if (score < 92.7) {
        return "low"
    }
    return "minimal"
}

fun adaptiveDeclarationWindowBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveDeclarationWindow(): Int {
    val policy = defaultPolicy("adaptive-declaration-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive declaration window code in its canonical ADW- form. */
fun normalizeAdaptiveDeclarationWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADW-")) {
        return clean
    }
    return "ADW-${clean.replace(" ", "-")}"
}
