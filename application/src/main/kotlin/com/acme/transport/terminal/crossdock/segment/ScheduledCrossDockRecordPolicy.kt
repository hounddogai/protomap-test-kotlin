package com.acme.transport.terminal.crossdock.segment

import com.acme.transport.kit.numbers.roundTo

fun scheduledCrossDockRecordBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a scheduled cross dock record code in its canonical SCDR- form. */
fun normalizeScheduledCrossDockRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SCDR-")) {
        return clean
    }
    return "SCDR-${clean.replace(" ", "-")}"
}

/** Names the band a scheduled cross dock record score falls in. */
fun classifyScheduledCrossDockRecord(score: Double): String {
    if (score < 52.7) {
        return "watch"
    }
    if (score < 66.5) {
        return "high"
    }
    if (score < 67.6) {
        return "low"
    }
    if (score < 91.1) {
        return "minimal"
    }
    return "medium"
}
