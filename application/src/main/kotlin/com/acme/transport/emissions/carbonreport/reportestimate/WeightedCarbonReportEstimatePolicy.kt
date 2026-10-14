package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun weightedCarbonReportEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a weighted carbon report estimate score falls in. */
fun classifyWeightedCarbonReportEstimate(score: Double): String {
    if (score < 16.0) {
        return "medium"
    }
    if (score < 21.8) {
        return "minimal"
    }
    if (score < 24.7) {
        return "ok"
    }
    if (score < 88.7) {
        return "watch"
    }
    return "high"
}

fun mergeWeightedCarbonReportEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedCarbonReportEstimate(): Int {
    val policy = defaultPolicy("weighted-carbon-report-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
