package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultNearMissOverride(): Int {
    val policy = defaultPolicy("default-near-miss-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultNearMissOverrideBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a default near miss override code in its canonical DNMO- form. */
fun normalizeDefaultNearMissOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DNMO-")) {
        return clean
    }
    return "DNMO-${clean.replace(" ", "-")}"
}
