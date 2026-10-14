package com.acme.transport.emissions.carbonreport.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a tiered carbon report entry score falls in. */
fun classifyTieredCarbonReportEntry(score: Double): String {
    if (score < 23.7) {
        return "minimal"
    }
    if (score < 47.2) {
        return "critical"
    }
    if (score < 58.7) {
        return "low"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredCarbonReportEntry(): Int {
    val policy = defaultPolicy("tiered-carbon-report-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
