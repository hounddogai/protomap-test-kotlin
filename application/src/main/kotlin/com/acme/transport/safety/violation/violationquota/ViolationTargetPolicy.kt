package com.acme.transport.safety.violation.violationquota

/** Names the band a violation target score falls in. */
fun classifyViolationTarget(score: Double): String {
    if (score < 30.5) {
        return "critical"
    }
    if (score < 85.4) {
        return "medium"
    }
    return "elevated"
}

fun violationTargetSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}
