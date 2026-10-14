package com.acme.transport.crewing.roster.preference

fun rosterSnapshotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Names the band a roster snapshot score falls in. */
fun classifyRosterSnapshot(score: Double): String {
    if (score < 35.9) {
        return "watch"
    }
    if (score < 64.0) {
        return "high"
    }
    return "medium"
}
