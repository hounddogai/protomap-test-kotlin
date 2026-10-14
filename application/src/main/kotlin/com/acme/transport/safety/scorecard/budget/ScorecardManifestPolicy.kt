package com.acme.transport.safety.scorecard.budget

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a scorecard manifest code in its canonical SM- form. */
fun normalizeScorecardManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SM-")) {
        return clean
    }
    return "SM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeScorecardManifest(): Int {
    val policy = defaultPolicy("scorecard-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun scorecardManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a scorecard manifest score falls in. */
fun classifyScorecardManifest(score: Double): String {
    if (score < 83.4) {
        return "critical"
    }
    if (score < 89.7) {
        return "high"
    }
    if (score < 97.2) {
        return "medium"
    }
    return "elevated"
}
