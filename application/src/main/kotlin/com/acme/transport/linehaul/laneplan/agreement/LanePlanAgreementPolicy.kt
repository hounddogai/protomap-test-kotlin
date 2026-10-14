package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanAgreement(): Int {
    val policy = defaultPolicy("lane-plan-agreement")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeLanePlanAgreementLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lane plan agreement score falls in. */
fun classifyLanePlanAgreement(score: Double): String {
    if (score < 33.3) {
        return "medium"
    }
    if (score < 91.0) {
        return "minimal"
    }
    return "high"
}
