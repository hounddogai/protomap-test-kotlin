package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a weighted lane plan bundle score falls in. */
fun classifyWeightedLanePlanBundle(score: Double): String {
    if (score < 20.2) {
        return "ok"
    }
    if (score < 41.2) {
        return "critical"
    }
    if (score < 78.9) {
        return "elevated"
    }
    if (score < 87.8) {
        return "low"
    }
    return "medium"
}

fun mergeWeightedLanePlanBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedLanePlanBundle(): Int {
    val policy = defaultPolicy("weighted-lane-plan-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
