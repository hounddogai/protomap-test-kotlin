package com.acme.transport.network.zone.plan

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 38756.0, in order. */
fun tieredZonePlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 38756.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeTieredZonePlanLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a tiered zone plan code in its canonical TZP- form. */
fun normalizeTieredZonePlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TZP-")) {
        return clean
    }
    return "TZP-${clean.replace(" ", "-")}"
}
