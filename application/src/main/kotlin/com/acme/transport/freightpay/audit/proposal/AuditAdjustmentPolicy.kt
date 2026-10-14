package com.acme.transport.freightpay.audit.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a audit adjustment code in its canonical AA- form. */
fun normalizeAuditAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AA-")) {
        return clean
    }
    return "AA-${clean.replace(" ", "-")}"
}

fun auditAdjustmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
