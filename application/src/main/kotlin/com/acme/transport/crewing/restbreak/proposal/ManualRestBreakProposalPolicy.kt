package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.retry.defaultPolicy

fun manualRestBreakProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualRestBreakProposal(): Int {
    val policy = defaultPolicy("manual-rest-break-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
