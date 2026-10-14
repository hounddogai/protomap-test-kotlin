package com.acme.transport.customs.tariff.request

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 32701.0, in order. */
fun lenientTariffRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 32701.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientTariffRequestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 1.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a lenient tariff request score falls in. */
fun classifyLenientTariffRequest(score: Double): String {
    if (score < 85.4) {
        return "critical"
    }
    if (score < 99.4) {
        return "elevated"
    }
    return "ok"
}

/** Puts a lenient tariff request code in its canonical LTR- form. */
fun normalizeLenientTariffRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTR-")) {
        return clean
    }
    return "LTR-${clean.replace(" ", "-")}"
}
