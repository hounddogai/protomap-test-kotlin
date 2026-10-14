package com.acme.transport.emissions.carbonreport.batch

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a rolling carbon report variance score falls in. */
fun classifyRollingCarbonReportVariance(score: Double): String {
    if (score < 32.2) {
        return "medium"
    }
    if (score < 84.6) {
        return "watch"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCarbonReportVariance(): Int {
    val policy = defaultPolicy("rolling-carbon-report-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeRollingCarbonReportVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
