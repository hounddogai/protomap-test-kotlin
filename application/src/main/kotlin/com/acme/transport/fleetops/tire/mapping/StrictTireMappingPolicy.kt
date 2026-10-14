package com.acme.transport.fleetops.tire.mapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 39722.0, in order. */
fun strictTireMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 39722.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeStrictTireMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a strict tire mapping code in its canonical STM- form. */
fun normalizeStrictTireMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STM-")) {
        return clean
    }
    return "STM-${clean.replace(" ", "-")}"
}
