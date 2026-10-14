package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientBackhaulAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a lenient backhaul audit code in its canonical LBA- form. */
fun normalizeLenientBackhaulAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LBA-")) {
        return clean
    }
    return "LBA-${clean.replace(" ", "-")}"
}
