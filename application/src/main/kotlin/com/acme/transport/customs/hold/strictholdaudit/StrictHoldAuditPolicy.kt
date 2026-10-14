package com.acme.transport.customs.hold.strictholdaudit

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun strictHoldAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a strict hold audit code in its canonical SHA- form. */
fun normalizeStrictHoldAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHA-")) {
        return clean
    }
    return "SHA-${clean.replace(" ", "-")}"
}
