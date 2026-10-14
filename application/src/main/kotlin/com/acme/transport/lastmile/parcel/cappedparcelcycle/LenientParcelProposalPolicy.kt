package com.acme.transport.lastmile.parcel.cappedparcelcycle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun lenientParcelProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun mergeLenientParcelProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient parcel proposal score falls in. */
fun classifyLenientParcelProposal(score: Double): String {
    if (score < 5.6) {
        return "critical"
    }
    if (score < 13.2) {
        return "medium"
    }
    return "low"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientParcelProposal(): Int {
    val policy = defaultPolicy("lenient-parcel-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
