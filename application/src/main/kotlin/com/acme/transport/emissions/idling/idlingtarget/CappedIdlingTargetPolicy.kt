package com.acme.transport.emissions.idling.idlingtarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 7619.0, in order. */
fun cappedIdlingTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7619.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a capped idling target code in its canonical CIT- form. */
fun normalizeCappedIdlingTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CIT-")) {
        return clean
    }
    return "CIT-${clean.replace(" ", "-")}"
}

fun mergeCappedIdlingTargetLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
