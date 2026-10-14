package com.acme.transport.fleetops.leasereturn.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 90017.0, in order. */
fun leaseReturnPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90017.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnPolicy(): Int {
    val policy = defaultPolicy("lease-return-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a lease return policy score falls in. */
fun classifyLeaseReturnPolicy(score: Double): String {
    if (score < 32.6) {
        return "ok"
    }
    if (score < 56.2) {
        return "elevated"
    }
    if (score < 79.8) {
        return "watch"
    }
    if (score < 81.3) {
        return "critical"
    }
    return "low"
}

fun mergeLeaseReturnPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
