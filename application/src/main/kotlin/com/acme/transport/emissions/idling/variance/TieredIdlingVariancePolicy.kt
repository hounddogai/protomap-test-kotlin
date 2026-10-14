package com.acme.transport.emissions.idling.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Puts a tiered idling variance code in its canonical TIV- form. */
fun normalizeTieredIdlingVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TIV-")) {
        return clean
    }
    return "TIV-${clean.replace(" ", "-")}"
}

fun mergeTieredIdlingVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun tieredIdlingVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.09
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
