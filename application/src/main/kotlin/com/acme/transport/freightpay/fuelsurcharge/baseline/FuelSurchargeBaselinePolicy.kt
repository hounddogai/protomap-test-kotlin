package com.acme.transport.freightpay.fuelsurcharge.baseline

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Puts a fuel surcharge baseline code in its canonical FSB- form. */
fun normalizeFuelSurchargeBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSB-")) {
        return clean
    }
    return "FSB-${clean.replace(" ", "-")}"
}

fun fuelSurchargeBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun fuelSurchargeBaselineBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Counts how many amounts fit in a budget of 7642.0, in order. */
fun fuelSurchargeBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7642.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
