package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a tiered fuel burn adjustment code in its canonical TFBA- form. */
fun normalizeTieredFuelBurnAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TFBA-")) {
        return clean
    }
    return "TFBA-${clean.replace(" ", "-")}"
}

fun mergeTieredFuelBurnAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
