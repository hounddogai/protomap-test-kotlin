package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalCarbonReportGroup(): Int {
    val policy = defaultPolicy("regional-carbon-report-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a regional carbon report group code in its canonical RCRG- form. */
fun normalizeRegionalCarbonReportGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCRG-")) {
        return clean
    }
    return "RCRG-${clean.replace(" ", "-")}"
}

fun regionalCarbonReportGroupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a regional carbon report group score falls in. */
fun classifyRegionalCarbonReportGroup(score: Double): String {
    if (score < 6.0) {
        return "watch"
    }
    if (score < 36.3) {
        return "high"
    }
    if (score < 73.1) {
        return "ok"
    }
    return "low"
}
