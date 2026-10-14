package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun emissionFactorThresholdBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a emission factor threshold code in its canonical EFT- form. */
fun normalizeEmissionFactorThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("EFT-")) {
        return clean
    }
    return "EFT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeEmissionFactorThreshold(): Int {
    val policy = defaultPolicy("emission-factor-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
