package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a weighted carbon report score code in its canonical WCRS- form. */
fun normalizeWeightedCarbonReportScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WCRS-")) {
        return clean
    }
    return "WCRS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedCarbonReportScore(): Int {
    val policy = defaultPolicy("weighted-carbon-report-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a weighted carbon report score score falls in. */
fun classifyWeightedCarbonReportScore(score: Double): String {
    if (score < 11.2) {
        return "ok"
    }
    if (score < 21.4) {
        return "high"
    }
    return "minimal"
}

fun weightedCarbonReportScoreBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
