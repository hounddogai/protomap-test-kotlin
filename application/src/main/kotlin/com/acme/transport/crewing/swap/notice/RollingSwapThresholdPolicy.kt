package com.acme.transport.crewing.swap.notice

import com.acme.transport.kit.numbers.roundTo

/** Puts a rolling swap threshold code in its canonical RST- form. */
fun normalizeRollingSwapThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RST-")) {
        return clean
    }
    return "RST-${clean.replace(" ", "-")}"
}

/** Names the band a rolling swap threshold score falls in. */
fun classifyRollingSwapThreshold(score: Double): String {
    if (score < 44.8) {
        return "low"
    }
    if (score < 50.3) {
        return "elevated"
    }
    if (score < 74.7) {
        return "minimal"
    }
    if (score < 90.0) {
        return "ok"
    }
    return "high"
}

fun rollingSwapThresholdBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
