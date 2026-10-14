package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a default scorecard notice code in its canonical DSN- form. */
fun normalizeDefaultScorecardNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DSN-")) {
        return clean
    }
    return "DSN-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultScorecardNotice(): Int {
    val policy = defaultPolicy("default-scorecard-notice")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
