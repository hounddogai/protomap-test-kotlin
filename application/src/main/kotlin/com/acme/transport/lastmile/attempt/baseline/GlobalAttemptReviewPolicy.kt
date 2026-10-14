package com.acme.transport.lastmile.attempt.baseline

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalAttemptReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun globalAttemptReviewSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

/** Puts a global attempt review code in its canonical GAR- form. */
fun normalizeGlobalAttemptReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GAR-")) {
        return clean
    }
    return "GAR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 66834.0, in order. */
fun globalAttemptReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66834.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
