package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a fuel burn window code in its canonical FBW- form. */
fun normalizeFuelBurnWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBW-")) {
        return clean
    }
    return "FBW-${clean.replace(" ", "-")}"
}

fun fuelBurnWindowWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 19582.0, in order. */
fun fuelBurnWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 19582.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
