package com.acme.transport.customs.inspection.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 43576.0, in order. */
fun weightedInspectionBundleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43576.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a weighted inspection bundle code in its canonical WIB- form. */
fun normalizeWeightedInspectionBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WIB-")) {
        return clean
    }
    return "WIB-${clean.replace(" ", "-")}"
}

fun mergeWeightedInspectionBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
