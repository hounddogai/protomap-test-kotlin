package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.numbers.roundTo

/** Puts a adaptive capacity entry code in its canonical ACE- form. */
fun normalizeAdaptiveCapacityEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ACE-")) {
        return clean
    }
    return "ACE-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 59777.0, in order. */
fun adaptiveCapacityEntryBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 59777.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveCapacityEntryBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
