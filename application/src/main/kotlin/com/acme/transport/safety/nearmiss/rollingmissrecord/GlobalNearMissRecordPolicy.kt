package com.acme.transport.safety.nearmiss.rollingmissrecord

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global near miss record code in its canonical GNMR- form. */
fun normalizeGlobalNearMissRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GNMR-")) {
        return clean
    }
    return "GNMR-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalNearMissRecord(): Int {
    val policy = defaultPolicy("global-near-miss-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
