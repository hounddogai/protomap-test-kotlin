package com.acme.transport.terminal.traileryard.yardentry

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling trailer yard policy code in its canonical RTYP- form. */
fun normalizeRollingTrailerYardPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTYP-")) {
        return clean
    }
    return "RTYP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingTrailerYardPolicy(): Int {
    val policy = defaultPolicy("rolling-trailer-yard-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingTrailerYardPolicyBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
