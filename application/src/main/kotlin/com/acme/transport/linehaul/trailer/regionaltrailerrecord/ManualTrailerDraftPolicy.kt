package com.acme.transport.linehaul.trailer.regionaltrailerrecord

/** Names the band a manual trailer draft score falls in. */
fun classifyManualTrailerDraft(score: Double): String {
    if (score < 22.2) {
        return "critical"
    }
    if (score < 33.0) {
        return "ok"
    }
    if (score < 50.8) {
        return "low"
    }
    return "high"
}

fun manualTrailerDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}
