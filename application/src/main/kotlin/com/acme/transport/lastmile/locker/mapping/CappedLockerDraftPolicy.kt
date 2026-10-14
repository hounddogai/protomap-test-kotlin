package com.acme.transport.lastmile.locker.mapping

fun cappedLockerDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a capped locker draft score falls in. */
fun classifyCappedLockerDraft(score: Double): String {
    if (score < 4.6) {
        return "low"
    }
    if (score < 37.6) {
        return "high"
    }
    if (score < 61.7) {
        return "elevated"
    }
    if (score < 64.7) {
        return "critical"
    }
    return "minimal"
}
