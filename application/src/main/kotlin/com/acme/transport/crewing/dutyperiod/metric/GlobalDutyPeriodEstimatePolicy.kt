package com.acme.transport.crewing.dutyperiod.metric

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalDutyPeriodEstimate(): Int {
    val policy = defaultPolicy("global-duty-period-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun globalDutyPeriodEstimateBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a global duty period estimate code in its canonical GDPE- form. */
fun normalizeGlobalDutyPeriodEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDPE-")) {
        return clean
    }
    return "GDPE-${clean.replace(" ", "-")}"
}

/** Names the band a global duty period estimate score falls in. */
fun classifyGlobalDutyPeriodEstimate(score: Double): String {
    if (score < 6.4) {
        return "high"
    }
    if (score < 24.4) {
        return "medium"
    }
    if (score < 37.3) {
        return "critical"
    }
    if (score < 77.0) {
        return "elevated"
    }
    return "ok"
}
