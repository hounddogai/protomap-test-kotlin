package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 93029.0, in order. */
fun serviceAreaReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93029.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a service area review code in its canonical SAR- form. */
fun normalizeServiceAreaReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SAR-")) {
        return clean
    }
    return "SAR-${clean.replace(" ", "-")}"
}

fun serviceAreaReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun mergeServiceAreaReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
