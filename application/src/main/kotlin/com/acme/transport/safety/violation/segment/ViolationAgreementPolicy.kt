package com.acme.transport.safety.violation.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationAgreement(): Int {
    val policy = defaultPolicy("violation-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun violationAgreementBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a violation agreement score falls in. */
fun classifyViolationAgreement(score: Double): String {
    if (score < 2.2) {
        return "minimal"
    }
    if (score < 5.7) {
        return "ok"
    }
    if (score < 9.0) {
        return "high"
    }
    if (score < 73.9) {
        return "critical"
    }
    return "low"
}

fun mergeViolationAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
