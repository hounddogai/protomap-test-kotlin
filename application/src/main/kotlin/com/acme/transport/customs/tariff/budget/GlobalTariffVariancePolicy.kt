package com.acme.transport.customs.tariff.budget

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun globalTariffVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.98
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a global tariff variance code in its canonical GTV- form. */
fun normalizeGlobalTariffVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GTV-")) {
        return clean
    }
    return "GTV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 16461.0, in order. */
fun globalTariffVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16461.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
