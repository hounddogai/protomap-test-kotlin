package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a seasonal near miss agreement code in its canonical SNMA- form. */
fun normalizeSeasonalNearMissAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SNMA-")) {
        return clean
    }
    return "SNMA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSeasonalNearMissAgreement(): Int {
    val policy = defaultPolicy("seasonal-near-miss-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
