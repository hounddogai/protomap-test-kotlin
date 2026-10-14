package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun regionalBackhaulBaselineBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a regional backhaul baseline code in its canonical RBB- form. */
fun normalizeRegionalBackhaulBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RBB-")) {
        return clean
    }
    return "RBB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalBackhaulBaseline(): Int {
    val policy = defaultPolicy("regional-backhaul-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
