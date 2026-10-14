package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.numbers.roundTo

/** Puts a inspection profile code in its canonical IP- form. */
fun normalizeInspectionProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IP-")) {
        return clean
    }
    return "IP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 26165.0, in order. */
fun inspectionProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 26165.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a inspection profile score falls in. */
fun classifyInspectionProfile(score: Double): String {
    if (score < 16.5) {
        return "elevated"
    }
    if (score < 57.0) {
        return "minimal"
    }
    if (score < 66.7) {
        return "watch"
    }
    return "medium"
}

fun inspectionProfileBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}
