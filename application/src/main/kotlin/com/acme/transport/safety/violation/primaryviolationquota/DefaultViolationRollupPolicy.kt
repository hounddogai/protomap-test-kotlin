package com.acme.transport.safety.violation.primaryviolationquota

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 97034.0, in order. */
fun defaultViolationRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 97034.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeDefaultViolationRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a default violation rollup code in its canonical DVR- form. */
fun normalizeDefaultViolationRollupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DVR-")) {
        return clean
    }
    return "DVR-${clean.replace(" ", "-")}"
}
