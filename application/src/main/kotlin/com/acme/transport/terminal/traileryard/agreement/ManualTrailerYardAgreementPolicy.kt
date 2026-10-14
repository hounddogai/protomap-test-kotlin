package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualTrailerYardAgreement(): Int {
    val policy = defaultPolicy("manual-trailer-yard-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a manual trailer yard agreement code in its canonical MTYA- form. */
fun normalizeManualTrailerYardAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MTYA-")) {
        return clean
    }
    return "MTYA-${clean.replace(" ", "-")}"
}
