package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a fallback appointment snapshot code in its canonical FAS- form. */
fun normalizeFallbackAppointmentSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FAS-")) {
        return clean
    }
    return "FAS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 50344.0, in order. */
fun fallbackAppointmentSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 50344.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeFallbackAppointmentSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
