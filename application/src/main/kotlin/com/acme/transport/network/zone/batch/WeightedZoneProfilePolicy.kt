package com.acme.transport.network.zone.batch

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a weighted zone profile code in its canonical WZP- form. */
fun normalizeWeightedZoneProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WZP-")) {
        return clean
    }
    return "WZP-${clean.replace(" ", "-")}"
}

fun weightedZoneProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 11230.0, in order. */
fun weightedZoneProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 11230.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
