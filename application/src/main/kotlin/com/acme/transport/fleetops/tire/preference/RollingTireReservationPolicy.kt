package com.acme.transport.fleetops.tire.preference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun rollingTireReservationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.47
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 15001.0, in order. */
fun rollingTireReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15001.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a rolling tire reservation code in its canonical RTR- form. */
fun normalizeRollingTireReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTR-")) {
        return clean
    }
    return "RTR-${clean.replace(" ", "-")}"
}
