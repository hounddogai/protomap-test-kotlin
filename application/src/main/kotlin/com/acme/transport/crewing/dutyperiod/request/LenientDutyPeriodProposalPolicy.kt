package com.acme.transport.crewing.dutyperiod.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeLenientDutyPeriodProposalLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientDutyPeriodProposal(): Int {
    val policy = defaultPolicy("lenient-duty-period-proposal")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lenient duty period proposal score falls in. */
fun classifyLenientDutyPeriodProposal(score: Double): String {
    if (score < 8.3) {
        return "high"
    }
    if (score < 57.7) {
        return "elevated"
    }
    if (score < 83.5) {
        return "low"
    }
    return "ok"
}
