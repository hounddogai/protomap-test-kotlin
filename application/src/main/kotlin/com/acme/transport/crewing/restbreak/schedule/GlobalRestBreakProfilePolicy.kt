package com.acme.transport.crewing.restbreak.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun mergeGlobalRestBreakProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun globalRestBreakProfileBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a global rest break profile code in its canonical GRBP- form. */
fun normalizeGlobalRestBreakProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GRBP-")) {
        return clean
    }
    return "GRBP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 86642.0, in order. */
fun globalRestBreakProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86642.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
