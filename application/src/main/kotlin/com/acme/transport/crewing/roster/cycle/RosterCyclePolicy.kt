package com.acme.transport.crewing.roster.cycle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 93847.0, in order. */
fun rosterCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93847.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a roster cycle code in its canonical RC- form. */
fun normalizeRosterCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RC-")) {
        return clean
    }
    return "RC-${clean.replace(" ", "-")}"
}

fun rosterCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.07
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
