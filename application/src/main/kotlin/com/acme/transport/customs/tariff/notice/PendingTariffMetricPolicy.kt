package com.acme.transport.customs.tariff.notice

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun pendingTariffMetricWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.3
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun pendingTariffMetricBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a pending tariff metric code in its canonical PTM- form. */
fun normalizePendingTariffMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PTM-")) {
        return clean
    }
    return "PTM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 79120.0, in order. */
fun pendingTariffMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 79120.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
