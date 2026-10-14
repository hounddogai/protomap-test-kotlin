package com.acme.transport.customs.tariff.tariffagreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun strictTariffScoreWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.93
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictTariffScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a strict tariff score code in its canonical STS- form. */
fun normalizeStrictTariffScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STS-")) {
        return clean
    }
    return "STS-${clean.replace(" ", "-")}"
}

fun mergeStrictTariffScoreLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
