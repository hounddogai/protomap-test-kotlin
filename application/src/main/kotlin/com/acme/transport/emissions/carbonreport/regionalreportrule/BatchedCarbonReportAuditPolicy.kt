package com.acme.transport.emissions.carbonreport.regionalreportrule

/** Names the band a batched carbon report audit score falls in. */
fun classifyBatchedCarbonReportAudit(score: Double): String {
    if (score < 23.3) {
        return "medium"
    }
    if (score < 29.0) {
        return "elevated"
    }
    if (score < 60.5) {
        return "ok"
    }
    if (score < 61.8) {
        return "critical"
    }
    return "high"
}

fun batchedCarbonReportAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
