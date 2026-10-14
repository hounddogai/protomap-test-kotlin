package com.acme.transport.freightpay.dispute.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a dispute slot code in its canonical DS- form. */
fun normalizeDisputeSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DS-")) {
        return clean
    }
    return "DS-${clean.replace(" ", "-")}"
}

fun disputeSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.48
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun disputeSlotBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a dispute slot score falls in. */
fun classifyDisputeSlot(score: Double): String {
    if (score < 14.7) {
        return "ok"
    }
    if (score < 50.1) {
        return "medium"
    }
    if (score < 89.6) {
        return "watch"
    }
    return "elevated"
}
