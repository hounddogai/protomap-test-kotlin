package com.acme.transport.linehaul.trailer.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a rolling trailer assignment code in its canonical RTA- form. */
fun normalizeRollingTrailerAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RTA-")) {
        return clean
    }
    return "RTA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 62711.0, in order. */
fun rollingTrailerAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 62711.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingTrailerAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.61
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
