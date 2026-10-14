package com.acme.transport.fleetops.tire.claim

import com.acme.transport.kit.numbers.roundTo

fun cappedTireTemplateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun cappedTireTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Names the band a capped tire template score falls in. */
fun classifyCappedTireTemplate(score: Double): String {
    if (score < 10.8) {
        return "ok"
    }
    if (score < 13.7) {
        return "low"
    }
    if (score < 97.4) {
        return "critical"
    }
    return "minimal"
}
