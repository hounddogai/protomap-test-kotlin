package com.acme.transport.crewing.qualification.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a qualification metric code in its canonical QM- form. */
fun normalizeQualificationMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("QM-")) {
        return clean
    }
    return "QM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationMetric(): Int {
    val policy = defaultPolicy("qualification-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
