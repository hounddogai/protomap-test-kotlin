package com.acme.transport.customs.inspection.allocation

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun inspectionEstimateBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeInspectionEstimate(): Int {
    val policy = defaultPolicy("inspection-estimate")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a inspection estimate code in its canonical IE- form. */
fun normalizeInspectionEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IE-")) {
        return clean
    }
    return "IE-${clean.replace(" ", "-")}"
}

/** Names the band a inspection estimate score falls in. */
fun classifyInspectionEstimate(score: Double): String {
    if (score < 5.9) {
        return "ok"
    }
    if (score < 22.5) {
        return "high"
    }
    return "medium"
}
