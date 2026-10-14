package com.acme.transport.fleetops.tire.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tiered tire cycle code in its canonical TTC- form. */
fun normalizeTieredTireCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTC-")) {
        return clean
    }
    return "TTC-${clean.replace(" ", "-")}"
}

fun tieredTireCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.71
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 54003.0, in order. */
fun tieredTireCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 54003.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
