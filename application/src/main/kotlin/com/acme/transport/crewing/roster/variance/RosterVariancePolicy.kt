package com.acme.transport.crewing.roster.variance

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a roster variance code in its canonical RV- form. */
fun normalizeRosterVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RV-")) {
        return clean
    }
    return "RV-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRosterVariance(): Int {
    val policy = defaultPolicy("roster-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
