package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.numbers.roundTo

fun fuelBurnQuotaBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Counts how many amounts fit in a budget of 37583.0, in order. */
fun fuelBurnQuotaBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37583.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelBurnQuotaSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a fuel burn quota code in its canonical FBQ- form. */
fun normalizeFuelBurnQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBQ-")) {
        return clean
    }
    return "FBQ-${clean.replace(" ", "-")}"
}
