package com.acme.transport.emissions.fuelburn.burnestimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a capped fuel burn milestone code in its canonical CFBM- form. */
fun normalizeCappedFuelBurnMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CFBM-")) {
        return clean
    }
    return "CFBM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 90026.0, in order. */
fun cappedFuelBurnMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90026.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun cappedFuelBurnMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.7
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
