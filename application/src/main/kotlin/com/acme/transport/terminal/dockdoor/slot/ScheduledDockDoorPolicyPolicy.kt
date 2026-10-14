package com.acme.transport.terminal.dockdoor.slot

import com.acme.transport.kit.numbers.roundTo

fun scheduledDockDoorPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a scheduled dock door policy code in its canonical SDDP- form. */
fun normalizeScheduledDockDoorPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDDP-")) {
        return clean
    }
    return "SDDP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 6221.0, in order. */
fun scheduledDockDoorPolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6221.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
