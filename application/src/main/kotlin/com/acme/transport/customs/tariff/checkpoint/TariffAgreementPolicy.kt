package com.acme.transport.customs.tariff.checkpoint

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tariff agreement code in its canonical TA- form. */
fun normalizeTariffAgreementCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TA-")) {
        return clean
    }
    return "TA-${clean.replace(" ", "-")}"
}

fun tariffAgreementSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}

fun tariffAgreementWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
