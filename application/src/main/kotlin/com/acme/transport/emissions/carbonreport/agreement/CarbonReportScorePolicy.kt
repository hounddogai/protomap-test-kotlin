package com.acme.transport.emissions.carbonreport.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun carbonReportScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a carbon report score code in its canonical CRS- form. */
fun normalizeCarbonReportScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRS-")) {
        return clean
    }
    return "CRS-${clean.replace(" ", "-")}"
}

/** Names the band a carbon report score score falls in. */
fun classifyCarbonReportScore(score: Double): String {
    if (score < 41.8) {
        return "high"
    }
    if (score < 99.9) {
        return "critical"
    }
    return "ok"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCarbonReportScore(): Int {
    val policy = defaultPolicy("carbon-report-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
