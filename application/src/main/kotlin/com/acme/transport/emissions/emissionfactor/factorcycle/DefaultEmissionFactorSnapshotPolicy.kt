package com.acme.transport.emissions.emissionfactor.factorcycle

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun defaultEmissionFactorSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultEmissionFactorSnapshot(): Int {
    val policy = defaultPolicy("default-emission-factor-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun defaultEmissionFactorSnapshotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a default emission factor snapshot code in its canonical DEFS- form. */
fun normalizeDefaultEmissionFactorSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DEFS-")) {
        return clean
    }
    return "DEFS-${clean.replace(" ", "-")}"
}
