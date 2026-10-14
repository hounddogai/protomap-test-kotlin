package com.acme.transport.customs.tariff.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

/** Puts a fallback tariff baseline code in its canonical FTB- form. */
fun normalizeFallbackTariffBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FTB-")) {
        return clean
    }
    return "FTB-${clean.replace(" ", "-")}"
}

fun fallbackTariffBaselineBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a fallback tariff baseline score falls in. */
fun classifyFallbackTariffBaseline(score: Double): String {
    if (score < 12.2) {
        return "minimal"
    }
    if (score < 75.8) {
        return "watch"
    }
    return "ok"
}

fun mergeFallbackTariffBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
