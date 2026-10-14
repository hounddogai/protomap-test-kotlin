package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun cappedCameraEventEntryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.67
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a capped camera event entry code in its canonical CCEE- form. */
fun normalizeCappedCameraEventEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCEE-")) {
        return clean
    }
    return "CCEE-${clean.replace(" ", "-")}"
}
