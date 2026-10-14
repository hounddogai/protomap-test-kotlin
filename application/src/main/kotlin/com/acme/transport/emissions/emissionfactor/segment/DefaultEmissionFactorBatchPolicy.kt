package com.acme.transport.emissions.emissionfactor.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeDefaultEmissionFactorBatchLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default emission factor batch code in its canonical DEFB- form. */
fun normalizeDefaultEmissionFactorBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DEFB-")) {
        return clean
    }
    return "DEFB-${clean.replace(" ", "-")}"
}

fun defaultEmissionFactorBatchBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}
