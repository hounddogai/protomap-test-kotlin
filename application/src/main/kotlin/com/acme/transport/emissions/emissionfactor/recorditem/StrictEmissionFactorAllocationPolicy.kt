package com.acme.transport.emissions.emissionfactor.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun strictEmissionFactorAllocationBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a strict emission factor allocation code in its canonical SEFA- form. */
fun normalizeStrictEmissionFactorAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SEFA-")) {
        return clean
    }
    return "SEFA-${clean.replace(" ", "-")}"
}

fun mergeStrictEmissionFactorAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
