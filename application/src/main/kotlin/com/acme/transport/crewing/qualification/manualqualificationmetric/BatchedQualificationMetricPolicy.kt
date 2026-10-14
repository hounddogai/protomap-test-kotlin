package com.acme.transport.crewing.qualification.manualqualificationmetric

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Puts a batched qualification metric code in its canonical BQM- form. */
fun normalizeBatchedQualificationMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BQM-")) {
        return clean
    }
    return "BQM-${clean.replace(" ", "-")}"
}

fun batchedQualificationMetricBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeBatchedQualificationMetric(): Int {
    val policy = defaultPolicy("batched-qualification-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
