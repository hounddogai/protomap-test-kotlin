package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeUtilizationProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a utilization proposal score falls in. */
fun classifyUtilizationProposal(score: Double): String {
    if (score < 21.2) {
        return "critical"
    }
    if (score < 44.6) {
        return "watch"
    }
    if (score < 79.8) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeUtilizationProposal(): Int {
    val policy = defaultPolicy("utilization-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
