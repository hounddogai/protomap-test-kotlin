package com.acme.transport.freightpay.audit.tier

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryAuditBundle(): Int {
    val policy = defaultPolicy("primary-audit-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary audit bundle score falls in. */
fun classifyPrimaryAuditBundle(score: Double): String {
    if (score < 29.2) {
        return "ok"
    }
    if (score < 30.8) {
        return "low"
    }
    if (score < 43.3) {
        return "medium"
    }
    if (score < 92.6) {
        return "high"
    }
    return "critical"
}

fun mergePrimaryAuditBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
