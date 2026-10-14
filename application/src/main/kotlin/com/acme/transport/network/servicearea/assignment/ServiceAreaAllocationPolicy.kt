package com.acme.transport.network.servicearea.assignment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a service area allocation code in its canonical SAA- form. */
fun normalizeServiceAreaAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAA-")) {
        return clean
    }
    return "SAA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeServiceAreaAllocation(): Int {
    val policy = defaultPolicy("service-area-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a service area allocation score falls in. */
fun classifyServiceAreaAllocation(score: Double): String {
    if (score < 12.1) {
        return "watch"
    }
    if (score < 14.7) {
        return "high"
    }
    if (score < 27.8) {
        return "minimal"
    }
    return "medium"
}

fun serviceAreaAllocationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
