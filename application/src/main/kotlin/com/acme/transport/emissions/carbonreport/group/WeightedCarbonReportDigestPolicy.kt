package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Names the band a weighted carbon report digest score falls in. */
fun classifyWeightedCarbonReportDigest(score: Double): String {
    if (score < 2.7) {
        return "high"
    }
    if (score < 6.2) {
        return "ok"
    }
    if (score < 25.8) {
        return "elevated"
    }
    if (score < 64.6) {
        return "critical"
    }
    return "medium"
}

fun weightedCarbonReportDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun weightedCarbonReportDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedCarbonReportDigest(): Int {
    val policy = defaultPolicy("weighted-carbon-report-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
