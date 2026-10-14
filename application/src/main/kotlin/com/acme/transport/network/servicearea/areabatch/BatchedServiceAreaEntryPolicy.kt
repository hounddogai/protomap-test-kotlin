package com.acme.transport.network.servicearea.areabatch

/** Names the band a batched service area entry score falls in. */
fun classifyBatchedServiceAreaEntry(score: Double): String {
    if (score < 17.0) {
        return "high"
    }
    if (score < 34.3) {
        return "elevated"
    }
    if (score < 94.0) {
        return "critical"
    }
    if (score < 97.4) {
        return "minimal"
    }
    return "watch"
}

fun batchedServiceAreaEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
