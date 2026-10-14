package com.acme.transport.network.cutoff.summary

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a cutoff summary code in its canonical CS- form. */
fun normalizeCutoffSummaryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CS-")) {
        return clean
    }
    return "CS-${clean.replace(" ", "-")}"
}

/** Names the band a cutoff summary score falls in. */
fun classifyCutoffSummary(score: Double): String {
    if (score < 1.9) {
        return "critical"
    }
    if (score < 42.1) {
        return "ok"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCutoffSummary(): Int {
    val policy = defaultPolicy("cutoff-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
