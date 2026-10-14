package com.acme.transport.customs.declaration.rule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalDeclarationAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

/** Puts a global declaration audit code in its canonical GDA- form. */
fun normalizeGlobalDeclarationAuditCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GDA-")) {
        return clean
    }
    return "GDA-${clean.replace(" ", "-")}"
}

fun globalDeclarationAuditWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.35
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
