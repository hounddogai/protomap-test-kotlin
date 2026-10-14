package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun mergeWeightedHubPlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedHubPlan(): Int {
    val policy = defaultPolicy("weighted-hub-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedHubPlanBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a weighted hub plan score falls in. */
fun classifyWeightedHubPlan(score: Double): String {
    if (score < 5.0) {
        return "critical"
    }
    if (score < 41.4) {
        return "medium"
    }
    return "low"
}
