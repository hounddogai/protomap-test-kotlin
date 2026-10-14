package com.acme.transport.safety.scorecard.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a scorecard variance code in its canonical SV- form. */
fun normalizeScorecardVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SV-")) {
        return clean
    }
    return "SV-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardVariance(): Int {
    val policy = defaultPolicy("scorecard-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
