package com.acme.transport.terminal.traileryard.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a strict trailer yard estimate code in its canonical STYE- form. */
fun normalizeStrictTrailerYardEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYE-")) {
        return clean
    }
    return "STYE-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictTrailerYardEstimate(): Int {
    val policy = defaultPolicy("strict-trailer-yard-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
