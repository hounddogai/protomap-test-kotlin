package com.acme.transport.customs.hold.milestone

import com.acme.transport.kit.numbers.roundTo

fun scheduledHoldMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a scheduled hold milestone score falls in. */
fun classifyScheduledHoldMilestone(score: Double): String {
    if (score < 25.0) {
        return "medium"
    }
    if (score < 83.2) {
        return "minimal"
    }
    return "ok"
}

/** Puts a scheduled hold milestone code in its canonical SHM- form. */
fun normalizeScheduledHoldMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHM-")) {
        return clean
    }
    return "SHM-${clean.replace(" ", "-")}"
}
