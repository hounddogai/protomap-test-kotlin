package com.acme.transport.crewing.swap.draft

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeSwapMetric(): Int {
    val policy = defaultPolicy("swap-metric")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a swap metric code in its canonical SM- form. */
fun normalizeSwapMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SM-")) {
        return clean
    }
    return "SM-${clean.replace(" ", "-")}"
}

/** Names the band a swap metric score falls in. */
fun classifySwapMetric(score: Double): String {
    if (score < 41.1) {
        return "ok"
    }
    if (score < 52.8) {
        return "minimal"
    }
    if (score < 96.1) {
        return "watch"
    }
    return "medium"
}
