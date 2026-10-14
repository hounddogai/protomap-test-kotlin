package com.acme.transport.crewing.swap.recorditem

/** Names the band a legacy swap score score falls in. */
fun classifyLegacySwapScore(score: Double): String {
    if (score < 6.5) {
        return "medium"
    }
    if (score < 42.3) {
        return "low"
    }
    if (score < 69.5) {
        return "ok"
    }
    return "watch"
}

fun legacySwapScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
