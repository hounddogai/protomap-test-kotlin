package com.acme.transport.crewing.swap.entry

/** Names the band a weighted swap segment score falls in. */
fun classifyWeightedSwapSegment(score: Double): String {
    if (score < 47.0) {
        return "minimal"
    }
    if (score < 67.0) {
        return "critical"
    }
    return "high"
}

fun weightedSwapSegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
