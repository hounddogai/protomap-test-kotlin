package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a scorecard allocation score falls in. */
fun classifyScorecardAllocation(score: Double): String {
    if (score < 23.4) {
        return "medium"
    }
    if (score < 44.2) {
        return "elevated"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardAllocation(): Int {
    val policy = defaultPolicy("scorecard-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scorecardAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Puts a scorecard allocation code in its canonical SA- form. */
fun normalizeScorecardAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SA-")) {
        return clean
    }
    return "SA-${clean.replace(" ", "-")}"
}
