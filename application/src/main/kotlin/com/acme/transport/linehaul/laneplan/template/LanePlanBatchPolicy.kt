package com.acme.transport.linehaul.laneplan.template

/** Names the band a lane plan batch score falls in. */
fun classifyLanePlanBatch(score: Double): String {
    if (score < 2.6) {
        return "high"
    }
    if (score < 34.0) {
        return "low"
    }
    if (score < 39.9) {
        return "critical"
    }
    return "medium"
}

fun lanePlanBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}
