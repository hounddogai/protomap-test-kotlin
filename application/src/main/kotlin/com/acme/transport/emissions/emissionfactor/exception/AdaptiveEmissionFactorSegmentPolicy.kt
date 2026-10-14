package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive emission factor segment code in its canonical AEFS- form. */
fun normalizeAdaptiveEmissionFactorSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AEFS-")) {
        return clean
    }
    return "AEFS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveEmissionFactorSegment(): Int {
    val policy = defaultPolicy("adaptive-emission-factor-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun adaptiveEmissionFactorSegmentBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
