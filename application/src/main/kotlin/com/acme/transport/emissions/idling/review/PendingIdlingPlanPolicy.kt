package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingIdlingPlan(): Int {
    val policy = defaultPolicy("pending-idling-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a pending idling plan score falls in. */
fun classifyPendingIdlingPlan(score: Double): String {
    if (score < 39.5) {
        return "low"
    }
    if (score < 43.3) {
        return "ok"
    }
    if (score < 53.0) {
        return "watch"
    }
    return "minimal"
}

fun mergePendingIdlingPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
