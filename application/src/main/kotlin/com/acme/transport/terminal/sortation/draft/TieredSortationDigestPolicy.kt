package com.acme.transport.terminal.sortation.draft

/** Names the band a tiered sortation digest score falls in. */
fun classifyTieredSortationDigest(score: Double): String {
    if (score < 2.8) {
        return "low"
    }
    if (score < 30.3) {
        return "elevated"
    }
    return "medium"
}

fun tieredSortationDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
