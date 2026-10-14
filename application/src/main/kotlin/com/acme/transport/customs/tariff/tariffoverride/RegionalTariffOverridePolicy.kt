package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a regional tariff override code in its canonical RTO- form. */
fun normalizeRegionalTariffOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTO-")) {
        return clean
    }
    return "RTO-${clean.replace(" ", "-")}"
}

fun regionalTariffOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.83
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
