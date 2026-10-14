package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun carbonReportSummaryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a carbon report summary score falls in. */
fun classifyCarbonReportSummary(score: Double): String {
    if (score < 23.0) {
        return "high"
    }
    if (score < 66.6) {
        return "ok"
    }
    if (score < 80.4) {
        return "low"
    }
    if (score < 92.5) {
        return "minimal"
    }
    return "watch"
}

fun mergeCarbonReportSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCarbonReportSummary(): Int {
    val policy = defaultPolicy("carbon-report-summary")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
