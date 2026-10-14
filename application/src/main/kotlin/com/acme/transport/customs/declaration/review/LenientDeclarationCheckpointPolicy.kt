package com.acme.transport.customs.declaration.review

fun lenientDeclarationCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Names the band a lenient declaration checkpoint score falls in. */
fun classifyLenientDeclarationCheckpoint(score: Double): String {
    if (score < 32.1) {
        return "elevated"
    }
    if (score < 48.0) {
        return "ok"
    }
    return "critical"
}
