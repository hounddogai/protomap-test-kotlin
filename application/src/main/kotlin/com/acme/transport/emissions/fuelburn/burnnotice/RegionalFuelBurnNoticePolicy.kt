package com.acme.transport.emissions.fuelburn.burnnotice

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRegionalFuelBurnNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a regional fuel burn notice code in its canonical RFBN- form. */
fun normalizeRegionalFuelBurnNoticeCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RFBN-")) {
        return clean
    }
    return "RFBN-${clean.replace(" ", "-")}"
}
