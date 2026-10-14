package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 28583.0, in order. */
fun fallbackAppointmentBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 28583.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fallback appointment baseline code in its canonical FAB- form. */
fun normalizeFallbackAppointmentBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FAB-")) {
        return clean
    }
    return "FAB-${clean.replace(" ", "-")}"
}

fun mergeFallbackAppointmentBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
