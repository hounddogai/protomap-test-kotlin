package com.acme.transport.freightpay.ratecard.cardgroup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun legacyRateCardGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a legacy rate card group code in its canonical LRCG- form. */
fun normalizeLegacyRateCardGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRCG-")) {
        return clean
    }
    return "LRCG-${clean.replace(" ", "-")}"
}

fun legacyRateCardGroupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
