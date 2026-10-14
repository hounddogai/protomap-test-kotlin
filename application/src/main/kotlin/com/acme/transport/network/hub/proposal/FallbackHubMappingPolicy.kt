package com.acme.transport.network.hub.proposal

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeFallbackHubMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 51528.0, in order. */
fun fallbackHubMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 51528.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fallbackHubMappingBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Puts a fallback hub mapping code in its canonical FHM- form. */
fun normalizeFallbackHubMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FHM-")) {
        return clean
    }
    return "FHM-${clean.replace(" ", "-")}"
}
