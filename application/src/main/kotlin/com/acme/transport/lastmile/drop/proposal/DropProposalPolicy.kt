package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.kit.retry.defaultPolicy

fun dropProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDropProposal(): Int {
    val policy = defaultPolicy("drop-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a drop proposal score falls in. */
fun classifyDropProposal(score: Double): String {
    if (score < 13.2) {
        return "watch"
    }
    if (score < 40.8) {
        return "medium"
    }
    if (score < 74.7) {
        return "high"
    }
    return "low"
}

/** Puts a drop proposal code in its canonical DP- form. */
fun normalizeDropProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}
