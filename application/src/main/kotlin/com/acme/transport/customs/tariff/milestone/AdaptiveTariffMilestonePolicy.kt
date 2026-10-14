package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.numbers.roundTo

/** Puts a adaptive tariff milestone code in its canonical ATM- form. */
fun normalizeAdaptiveTariffMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ATM-")) {
        return clean
    }
    return "ATM-${clean.replace(" ", "-")}"
}

fun adaptiveTariffMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
