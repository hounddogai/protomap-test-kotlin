package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fallbackZoneSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

fun fallbackZoneSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.5
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a fallback zone slot code in its canonical FZS- form. */
fun normalizeFallbackZoneSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FZS-")) {
        return clean
    }
    return "FZS-${clean.replace(" ", "-")}"
}
