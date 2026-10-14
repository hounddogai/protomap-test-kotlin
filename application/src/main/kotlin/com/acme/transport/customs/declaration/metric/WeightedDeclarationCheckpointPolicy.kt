package com.acme.transport.customs.declaration.metric

fun weightedDeclarationCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Names the band a weighted declaration checkpoint score falls in. */
fun classifyWeightedDeclarationCheckpoint(score: Double): String {
    if (score < 9.4) {
        return "elevated"
    }
    if (score < 29.8) {
        return "minimal"
    }
    if (score < 34.1) {
        return "ok"
    }
    if (score < 57.7) {
        return "low"
    }
    return "watch"
}
