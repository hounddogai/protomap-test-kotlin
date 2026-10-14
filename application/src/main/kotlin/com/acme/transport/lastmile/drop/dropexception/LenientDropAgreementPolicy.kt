package com.acme.transport.lastmile.drop.dropexception

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun lenientDropAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.37
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a lenient drop agreement code in its canonical LDA- form. */
fun normalizeLenientDropAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LDA-")) {
        return clean
    }
    return "LDA-${clean.replace(" ", "-")}"
}
