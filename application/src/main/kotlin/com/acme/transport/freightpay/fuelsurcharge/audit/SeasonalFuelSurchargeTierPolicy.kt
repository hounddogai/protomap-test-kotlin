package com.acme.transport.freightpay.fuelsurcharge.audit

import com.acme.transport.kit.numbers.roundTo

fun seasonalFuelSurchargeTierBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a seasonal fuel surcharge tier score falls in. */
fun classifySeasonalFuelSurchargeTier(score: Double): String {
    if (score < 18.5) {
        return "ok"
    }
    if (score < 29.5) {
        return "minimal"
    }
    return "watch"
}

/** Puts a seasonal fuel surcharge tier code in its canonical SFST- form. */
fun normalizeSeasonalFuelSurchargeTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SFST-")) {
        return clean
    }
    return "SFST-${clean.replace(" ", "-")}"
}
