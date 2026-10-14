package com.acme.transport.fleetops.tire.target

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a tire threshold code in its canonical TT- form. */
fun normalizeTireThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TT-")) {
        return clean
    }
    return "TT-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 96523.0, in order. */
fun tireThresholdBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 96523.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun tireThresholdWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.84
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
