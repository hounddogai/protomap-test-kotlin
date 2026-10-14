package com.acme.transport.customs.tariff.tariffscore

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveTariffScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.9
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a adaptive tariff score code in its canonical ATS- form. */
fun normalizeAdaptiveTariffScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ATS-")) {
        return clean
    }
    return "ATS-${clean.replace(" ", "-")}"
}
