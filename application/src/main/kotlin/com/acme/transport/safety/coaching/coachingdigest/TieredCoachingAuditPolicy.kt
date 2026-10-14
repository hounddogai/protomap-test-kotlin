package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredCoachingAudit(): Int {
    val policy = defaultPolicy("tiered-coaching-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeTieredCoachingAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a tiered coaching audit score falls in. */
fun classifyTieredCoachingAudit(score: Double): String {
    if (score < 26.9) {
        return "watch"
    }
    if (score < 99.0) {
        return "low"
    }
    return "critical"
}
