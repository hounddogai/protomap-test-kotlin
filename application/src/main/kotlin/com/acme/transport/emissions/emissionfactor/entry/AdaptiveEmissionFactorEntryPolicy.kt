package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveEmissionFactorEntry(): Int {
    val policy = defaultPolicy("adaptive-emission-factor-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive emission factor entry code in its canonical AEFE- form. */
fun normalizeAdaptiveEmissionFactorEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AEFE-")) {
        return clean
    }
    return "AEFE-${clean.replace(" ", "-")}"
}

fun adaptiveEmissionFactorEntryBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun adaptiveEmissionFactorEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
