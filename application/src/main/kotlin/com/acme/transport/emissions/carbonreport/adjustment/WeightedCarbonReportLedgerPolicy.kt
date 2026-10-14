package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedCarbonReportLedger(): Int {
    val policy = defaultPolicy("weighted-carbon-report-ledger")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun weightedCarbonReportLedgerWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.0
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a weighted carbon report ledger score falls in. */
fun classifyWeightedCarbonReportLedger(score: Double): String {
    if (score < 9.0) {
        return "high"
    }
    if (score < 52.9) {
        return "elevated"
    }
    if (score < 85.1) {
        return "watch"
    }
    if (score < 95.1) {
        return "critical"
    }
    return "medium"
}
