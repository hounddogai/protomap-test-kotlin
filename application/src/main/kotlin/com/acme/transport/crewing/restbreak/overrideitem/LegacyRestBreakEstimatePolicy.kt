package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 76768.0, in order. */
fun legacyRestBreakEstimateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 76768.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy rest break estimate code in its canonical LRBE- form. */
fun normalizeLegacyRestBreakEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRBE-")) {
        return clean
    }
    return "LRBE-${clean.replace(" ", "-")}"
}

fun mergeLegacyRestBreakEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
