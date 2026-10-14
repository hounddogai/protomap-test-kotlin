package com.acme.transport.freightpay.audit.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeAuditPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a audit policy score falls in. */
fun classifyAuditPolicy(score: Double): String {
    if (score < 13.4) {
        return "watch"
    }
    if (score < 92.6) {
        return "minimal"
    }
    if (score < 98.1) {
        return "critical"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAuditPolicy(): Int {
    val policy = defaultPolicy("audit-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
