package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a primary carbon report cycle score falls in. */
fun classifyPrimaryCarbonReportCycle(score: Double): String {
    if (score < 44.0) {
        return "low"
    }
    if (score < 50.8) {
        return "minimal"
    }
    if (score < 70.7) {
        return "ok"
    }
    if (score < 77.5) {
        return "elevated"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryCarbonReportCycle(): Int {
    val policy = defaultPolicy("primary-carbon-report-cycle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergePrimaryCarbonReportCycleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryCarbonReportCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
