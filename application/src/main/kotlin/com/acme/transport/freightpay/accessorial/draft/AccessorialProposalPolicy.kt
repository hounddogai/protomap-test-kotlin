package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAccessorialProposal(): Int {
    val policy = defaultPolicy("accessorial-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a accessorial proposal score falls in. */
fun classifyAccessorialProposal(score: Double): String {
    if (score < 25.6) {
        return "ok"
    }
    if (score < 42.3) {
        return "medium"
    }
    if (score < 47.3) {
        return "high"
    }
    if (score < 91.3) {
        return "critical"
    }
    return "elevated"
}

fun accessorialProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}
