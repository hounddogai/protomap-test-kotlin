package com.acme.transport.freightpay.dispute.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 61036.0, in order. */
fun disputePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 61036.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeDisputePolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDisputePolicy(): Int {
    val policy = defaultPolicy("dispute-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a dispute policy score falls in. */
fun classifyDisputePolicy(score: Double): String {
    if (score < 24.9) {
        return "high"
    }
    if (score < 40.5) {
        return "watch"
    }
    if (score < 84.2) {
        return "medium"
    }
    if (score < 96.4) {
        return "elevated"
    }
    return "minimal"
}
