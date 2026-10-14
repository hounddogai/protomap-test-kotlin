package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a global tender draft code in its canonical GTD- form. */
fun normalizeGlobalTenderDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTD-")) {
        return clean
    }
    return "GTD-${clean.replace(" ", "-")}"
}

fun globalTenderDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.15
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
