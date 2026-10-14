package com.acme.transport.crewing.roster.claim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRosterProfile(): Int {
    val policy = defaultPolicy("roster-profile")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a roster profile code in its canonical RP- form. */
fun normalizeRosterProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RP-")) {
        return clean
    }
    return "RP-${clean.replace(" ", "-")}"
}

fun rosterProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
