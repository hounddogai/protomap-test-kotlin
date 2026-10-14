package com.acme.transport.customs.hold.holdmapping

import com.acme.transport.kit.numbers.roundTo

fun scheduledHoldMappingBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 22760.0, in order. */
fun scheduledHoldMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22760.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled hold mapping code in its canonical SHM- form. */
fun normalizeScheduledHoldMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHM-")) {
        return clean
    }
    return "SHM-${clean.replace(" ", "-")}"
}
