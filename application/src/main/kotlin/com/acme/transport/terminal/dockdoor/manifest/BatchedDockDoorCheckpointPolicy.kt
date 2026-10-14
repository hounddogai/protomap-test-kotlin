package com.acme.transport.terminal.dockdoor.manifest

/** Names the band a batched dock door checkpoint score falls in. */
fun classifyBatchedDockDoorCheckpoint(score: Double): String {
    if (score < 30.4) {
        return "medium"
    }
    if (score < 65.4) {
        return "watch"
    }
    if (score < 76.4) {
        return "minimal"
    }
    if (score < 89.2) {
        return "high"
    }
    return "ok"
}

fun batchedDockDoorCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}
