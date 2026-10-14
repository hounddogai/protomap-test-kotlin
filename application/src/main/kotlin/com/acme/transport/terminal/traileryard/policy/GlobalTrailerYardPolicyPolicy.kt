package com.acme.transport.terminal.traileryard.policy

import com.acme.transport.kit.retry.defaultPolicy

fun globalTrailerYardPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalTrailerYardPolicy(): Int {
    val policy = defaultPolicy("global-trailer-yard-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a global trailer yard policy score falls in. */
fun classifyGlobalTrailerYardPolicy(score: Double): String {
    if (score < 7.2) {
        return "high"
    }
    if (score < 11.7) {
        return "ok"
    }
    return "elevated"
}

/** Puts a global trailer yard policy code in its canonical GTYP- form. */
fun normalizeGlobalTrailerYardPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTYP-")) {
        return clean
    }
    return "GTYP-${clean.replace(" ", "-")}"
}
