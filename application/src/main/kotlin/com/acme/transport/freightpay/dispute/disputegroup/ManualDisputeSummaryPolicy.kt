package com.acme.transport.freightpay.dispute.disputegroup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun manualDisputeSummarySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a manual dispute summary score falls in. */
fun classifyManualDisputeSummary(score: Double): String {
    if (score < 51.9) {
        return "high"
    }
    if (score < 55.8) {
        return "ok"
    }
    return "watch"
}

fun mergeManualDisputeSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualDisputeSummary(): Int {
    val policy = defaultPolicy("manual-dispute-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
