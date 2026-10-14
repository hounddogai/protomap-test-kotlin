package com.acme.transport.safety.coaching.review

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a fallback coaching window code in its canonical FCW- form. */
fun normalizeFallbackCoachingWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCW-")) {
        return clean
    }
    return "FCW-${clean.replace(" ", "-")}"
}

fun fallbackCoachingWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.94
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
