package com.acme.transport.fleetops.tire.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun tireBaselineBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun tireBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a tire baseline code in its canonical TB- form. */
fun normalizeTireBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TB-")) {
        return clean
    }
    return "TB-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 15021.0, in order. */
fun tireBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 15021.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
