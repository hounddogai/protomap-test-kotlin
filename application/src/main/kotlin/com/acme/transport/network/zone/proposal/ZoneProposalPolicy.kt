package com.acme.transport.network.zone.proposal

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeZoneProposal(): Int {
    val policy = defaultPolicy("zone-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a zone proposal score falls in. */
fun classifyZoneProposal(score: Double): String {
    if (score < 18.3) {
        return "medium"
    }
    if (score < 37.7) {
        return "elevated"
    }
    return "low"
}

fun zoneProposalBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun zoneProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
