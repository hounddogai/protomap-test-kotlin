package com.acme.transport.terminal.crossdock.profile

/** Names the band a legacy cross dock profile score falls in. */
fun classifyLegacyCrossDockProfile(score: Double): String {
    if (score < 31.1) {
        return "critical"
    }
    if (score < 35.2) {
        return "ok"
    }
    if (score < 42.3) {
        return "elevated"
    }
    return "high"
}

fun legacyCrossDockProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}
