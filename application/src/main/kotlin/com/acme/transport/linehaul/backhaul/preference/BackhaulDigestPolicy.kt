package com.acme.transport.linehaul.backhaul.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun backhaulDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

/** Names the band a backhaul digest score falls in. */
fun classifyBackhaulDigest(score: Double): String {
    if (score < 15.7) {
        return "critical"
    }
    if (score < 23.3) {
        return "low"
    }
    if (score < 74.9) {
        return "ok"
    }
    return "high"
}

/** Puts a backhaul digest code in its canonical BD- form. */
fun normalizeBackhaulDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BD-")) {
        return clean
    }
    return "BD-${clean.replace(" ", "-")}"
}

fun backhaulDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.91
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
