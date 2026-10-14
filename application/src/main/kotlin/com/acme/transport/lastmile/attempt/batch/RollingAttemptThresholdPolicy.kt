package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a rolling attempt threshold code in its canonical RAT- form. */
fun normalizeRollingAttemptThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RAT-")) {
        return clean
    }
    return "RAT-${clean.replace(" ", "-")}"
}

fun rollingAttemptThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun rollingAttemptThresholdBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a rolling attempt threshold score falls in. */
fun classifyRollingAttemptThreshold(score: Double): String {
    if (score < 21.3) {
        return "elevated"
    }
    if (score < 29.0) {
        return "medium"
    }
    if (score < 95.6) {
        return "high"
    }
    return "watch"
}
