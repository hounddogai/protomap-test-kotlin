package com.acme.transport.fleetops.serviceplan.planprofile

import com.acme.transport.kit.numbers.roundTo

fun batchedServicePlanTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Puts a batched service plan target code in its canonical BSPT- form. */
fun normalizeBatchedServicePlanTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BSPT-")) {
        return clean
    }
    return "BSPT-${clean.replace(" ", "-")}"
}

/** Names the band a batched service plan target score falls in. */
fun classifyBatchedServicePlanTarget(score: Double): String {
    if (score < 9.7) {
        return "medium"
    }
    if (score < 34.6) {
        return "elevated"
    }
    return "minimal"
}

fun batchedServicePlanTargetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
