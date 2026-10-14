package com.acme.transport.freightpay.dispute.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeLenientDisputeMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lenient dispute metric score falls in. */
fun classifyLenientDisputeMetric(score: Double): String {
    if (score < 7.4) {
        return "low"
    }
    if (score < 13.1) {
        return "medium"
    }
    if (score < 56.2) {
        return "minimal"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientDisputeMetric(): Int {
    val policy = defaultPolicy("lenient-dispute-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 19746.0, in order. */
fun lenientDisputeMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19746.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
