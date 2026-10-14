package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 90331.0, in order. */
fun regionalDockDoorTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90331.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a regional dock door tier score falls in. */
fun classifyRegionalDockDoorTier(score: Double): String {
    if (score < 59.2) {
        return "ok"
    }
    if (score < 65.6) {
        return "medium"
    }
    if (score < 77.9) {
        return "critical"
    }
    return "elevated"
}

fun regionalDockDoorTierBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a regional dock door tier code in its canonical RDDT- form. */
fun normalizeRegionalDockDoorTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDDT-")) {
        return clean
    }
    return "RDDT-${clean.replace(" ", "-")}"
}
