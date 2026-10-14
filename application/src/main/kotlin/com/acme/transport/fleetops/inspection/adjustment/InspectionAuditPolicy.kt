package com.acme.transport.fleetops.inspection.adjustment

fun inspectionAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

/** Names the band a inspection audit score falls in. */
fun classifyInspectionAudit(score: Double): String {
    if (score < 9.0) {
        return "ok"
    }
    if (score < 90.7) {
        return "high"
    }
    return "minimal"
}
