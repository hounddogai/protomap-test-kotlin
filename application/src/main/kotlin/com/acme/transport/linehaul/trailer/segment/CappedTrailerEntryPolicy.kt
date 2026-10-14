package com.acme.transport.linehaul.trailer.segment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a capped trailer entry code in its canonical CTE- form. */
fun normalizeCappedTrailerEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CTE-")) {
        return clean
    }
    return "CTE-${clean.replace(" ", "-")}"
}

fun cappedTrailerEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun cappedTrailerEntrySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a capped trailer entry score falls in. */
fun classifyCappedTrailerEntry(score: Double): String {
    if (score < 14.5) {
        return "elevated"
    }
    if (score < 32.2) {
        return "low"
    }
    if (score < 92.4) {
        return "medium"
    }
    return "watch"
}
