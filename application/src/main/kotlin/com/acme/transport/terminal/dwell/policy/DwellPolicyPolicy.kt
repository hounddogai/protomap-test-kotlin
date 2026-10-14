package com.acme.transport.terminal.dwell.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a dwell policy code in its canonical DP- form. */
fun normalizeDwellPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}

fun dwellPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Names the band a dwell policy score falls in. */
fun classifyDwellPolicy(score: Double): String {
    if (score < 32.2) {
        return "watch"
    }
    if (score < 57.3) {
        return "ok"
    }
    if (score < 88.2) {
        return "elevated"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDwellPolicy(): Int {
    val policy = defaultPolicy("dwell-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
