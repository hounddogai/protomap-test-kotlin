package com.acme.transport.network.servicearea.areaexception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a primary service area entry code in its canonical PSAE- form. */
fun normalizePrimaryServiceAreaEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PSAE-")) {
        return clean
    }
    return "PSAE-${clean.replace(" ", "-")}"
}

fun mergePrimaryServiceAreaEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 23025.0, in order. */
fun primaryServiceAreaEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23025.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
