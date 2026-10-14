package com.acme.transport.linehaul.trailer.template

import com.acme.transport.kit.numbers.roundTo

/** Names the band a manual trailer template score falls in. */
fun classifyManualTrailerTemplate(score: Double): String {
    if (score < 2.6) {
        return "critical"
    }
    if (score < 15.8) {
        return "ok"
    }
    if (score < 17.2) {
        return "medium"
    }
    if (score < 71.1) {
        return "minimal"
    }
    return "watch"
}

fun manualTrailerTemplateBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun manualTrailerTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
