package com.acme.transport.emissions.evcharge.agreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a ev charge exception code in its canonical ECE- form. */
fun normalizeEvChargeExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECE-")) {
        return clean
    }
    return "ECE-${clean.replace(" ", "-")}"
}

/** Names the band a ev charge exception score falls in. */
fun classifyEvChargeException(score: Double): String {
    if (score < 21.8) {
        return "critical"
    }
    if (score < 57.6) {
        return "minimal"
    }
    if (score < 79.1) {
        return "ok"
    }
    if (score < 90.2) {
        return "low"
    }
    return "watch"
}

fun evChargeExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun evChargeExceptionBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
