package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Names the band a pending clearance review score falls in. */
fun classifyPendingClearanceReview(score: Double): String {
    if (score < 13.6) {
        return "minimal"
    }
    if (score < 25.0) {
        return "critical"
    }
    return "low"
}

fun pendingClearanceReviewBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a pending clearance review code in its canonical PCR- form. */
fun normalizePendingClearanceReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCR-")) {
        return clean
    }
    return "PCR-${clean.replace(" ", "-")}"
}

fun pendingClearanceReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
