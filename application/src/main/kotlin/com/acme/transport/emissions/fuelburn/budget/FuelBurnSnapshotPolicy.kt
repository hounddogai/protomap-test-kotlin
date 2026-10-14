package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 98995.0, in order. */
fun fuelBurnSnapshotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98995.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fuel burn snapshot code in its canonical FBS- form. */
fun normalizeFuelBurnSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBS-")) {
        return clean
    }
    return "FBS-${clean.replace(" ", "-")}"
}

fun fuelBurnSnapshotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.01
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
