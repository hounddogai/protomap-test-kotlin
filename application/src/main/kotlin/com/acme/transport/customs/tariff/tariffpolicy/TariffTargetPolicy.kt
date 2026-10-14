package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.numbers.roundTo

fun tariffTargetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a tariff target code in its canonical TT- form. */
fun normalizeTariffTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TT-")) {
        return clean
    }
    return "TT-${clean.replace(" ", "-")}"
}

/** Names the band a tariff target score falls in. */
fun classifyTariffTarget(score: Double): String {
    if (score < 6.6) {
        return "critical"
    }
    if (score < 39.9) {
        return "elevated"
    }
    if (score < 43.6) {
        return "high"
    }
    if (score < 87.0) {
        return "ok"
    }
    return "medium"
}
