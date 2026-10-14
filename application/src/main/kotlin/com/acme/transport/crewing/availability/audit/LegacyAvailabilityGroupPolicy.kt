package com.acme.transport.crewing.availability.audit

import com.acme.transport.kit.numbers.roundTo

fun legacyAvailabilityGroupBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a legacy availability group code in its canonical LAG- form. */
fun normalizeLegacyAvailabilityGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAG-")) {
        return clean
    }
    return "LAG-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 85220.0, in order. */
fun legacyAvailabilityGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 85220.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
