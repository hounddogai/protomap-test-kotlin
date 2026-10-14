package com.acme.transport.safety.nearmiss.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a rolling near miss segment code in its canonical RNMS- form. */
fun normalizeRollingNearMissSegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RNMS-")) {
        return clean
    }
    return "RNMS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingNearMissSegment(): Int {
    val policy = defaultPolicy("rolling-near-miss-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
