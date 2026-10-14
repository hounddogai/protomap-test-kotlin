package com.acme.transport.customs.tariff.globaltariffpolicy

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a rolling tariff checkpoint code in its canonical RTC- form. */
fun normalizeRollingTariffCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTC-")) {
        return clean
    }
    return "RTC-${clean.replace(" ", "-")}"
}

fun rollingTariffCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.6
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
