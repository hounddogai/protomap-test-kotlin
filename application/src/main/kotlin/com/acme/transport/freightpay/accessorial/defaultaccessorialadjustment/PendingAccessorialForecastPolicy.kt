package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 21018.0, in order. */
fun pendingAccessorialForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21018.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePendingAccessorialForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending accessorial forecast code in its canonical PAF- form. */
fun normalizePendingAccessorialForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAF-")) {
        return clean
    }
    return "PAF-${clean.replace(" ", "-")}"
}

fun pendingAccessorialForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}
