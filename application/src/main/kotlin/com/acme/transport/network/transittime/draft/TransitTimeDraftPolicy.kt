package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a transit time draft code in its canonical TTD- form. */
fun normalizeTransitTimeDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTD-")) {
        return clean
    }
    return "TTD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 21480.0, in order. */
fun transitTimeDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21480.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun transitTimeDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergeTransitTimeDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
