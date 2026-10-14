package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a global trailer yard milestone code in its canonical GTYM- form. */
fun normalizeGlobalTrailerYardMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTYM-")) {
        return clean
    }
    return "GTYM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalTrailerYardMilestone(): Int {
    val policy = defaultPolicy("global-trailer-yard-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
