package com.acme.transport.fleetops.tire.tireclaim

/** Names the band a batched tire threshold score falls in. */
fun classifyBatchedTireThreshold(score: Double): String {
    if (score < 2.9) {
        return "critical"
    }
    if (score < 54.0) {
        return "minimal"
    }
    if (score < 58.3) {
        return "high"
    }
    if (score < 77.5) {
        return "low"
    }
    return "medium"
}

fun batchedTireThresholdSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
