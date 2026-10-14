package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalFuelSurchargeBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.96
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 90125.0, in order. */
fun globalFuelSurchargeBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 90125.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a global fuel surcharge batch code in its canonical GFSB- form. */
fun normalizeGlobalFuelSurchargeBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GFSB-")) {
        return clean
    }
    return "GFSB-${clean.replace(" ", "-")}"
}
