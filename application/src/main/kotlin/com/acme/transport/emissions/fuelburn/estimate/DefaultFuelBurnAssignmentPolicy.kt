package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeDefaultFuelBurnAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default fuel burn assignment code in its canonical DFBA- form. */
fun normalizeDefaultFuelBurnAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DFBA-")) {
        return clean
    }
    return "DFBA-${clean.replace(" ", "-")}"
}

fun defaultFuelBurnAssignmentBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
